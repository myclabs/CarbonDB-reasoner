package com.mycsense.carbondb; 

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import java.io.*;
import java.io.FileOutputStream;
import com.hp.hpl.jena.util.FileManager;
import java.util.ArrayList;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.matrix.Matrix;
import org.la4j.inversion.MatrixInverter;
import org.la4j.LinearAlgebra;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.reasoner.*;

public class Calculation {

    //protected Model model;
    protected Model model;
    protected InfModel infModel;
    protected ArrayList<Resource> processes;
    protected Matrix a;
    protected Matrix b;
    protected Matrix c;
    protected Matrix d;
    protected ArrayList<Resource> elementaryFlowNatures;

    Calculation (String inputFileName) {
        model = ModelFactory.createDefaultModel();

        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException(
                        "File: " + inputFileName + " not found");
        }

        model.read(in, null);

        extractProcesses();
        extractElementaryNatures();
    }

    Calculation (Model model) {
        this.model = model;

        extractProcesses();
        extractElementaryNatures();
    }

    public void run()
    {
        createMatrix();
        inverseMatrix();
        createEcologicalMatrix();
        calculateCumulatedEcologicalFlows();
        createCumulatedEcologicalFlows();
    }

    public void calculateCumulatedEcologicalFlows()
    {
        // We will use Gauss-Jordan method for inverting
        MatrixInverter inverter = a.withInverter(LinearAlgebra.GAUSS_JORDAN);
        // The 'b' matrix will be dense
        c = inverter.inverse(LinearAlgebra.DENSE_FACTORY);

        d = c.multiply(b);

        System.out.println(d);

    }

    public void extractProcesses() {
        System.out.println("extracting processes");
        Property singleProcess = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "SingleProcess");
        //ResIterator iter = model.listSubjectsWithProperty(singleProcess);
        //Selector selector = new SimpleSelector(null, RDF.type, "http://www.myc-sense.com/ontologies/bc#singleProcess");
        //Selector selector = new SimpleSelector(null, RDF.type, (RDFNode) null);
        Selector selector = new SimpleSelector(null, RDF.type, (RDFNode) singleProcess);
        StmtIterator iter = model.listStatements( selector );
        processes = new ArrayList<Resource>();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                //Resource r = iter.nextResource();
                Statement s = iter.nextStatement();
                System.out.println("process found : " + s.getSubject());
                processes.add(s.getSubject());
            }
            System.out.println("number of processes: " + processes.size());
        }
        else {
            System.out.println("no process found");
        }
    }

    public void createMatrix() {
        Property hasDestination = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "hasDestination");
        Property hasOrigin = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "hasOrigin");

        a = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < processes.size(); i++) {
            System.out.println("fetching data for process " + processes.get(i) + "(" + i + ")" + " (number of processes : " + processes.size() + ")");
            a.set(i, i, 1.0);
            ArrayList<Resource> relations = findRelations(processes.get(i));
            for (Resource relation : relations) {
                RDFNode downStreamProcess = relation.getProperty(hasDestination).getResource();
                // getCoefficient(processes.get(i), downStreamProcess)
                a.set(processes.indexOf(downStreamProcess), i, -getCoefficient(relation));
            }
        }
        System.out.println(a);
    }

    public void createEcologicalMatrix() {
        Property emits = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "emits");
        Property hasNature = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "hasNature");
        Property value = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "value");

        b = new CCSMatrix(processes.size(), elementaryFlowNatures.size());

        for (int i = 0; i < processes.size(); i++) {
            StmtIterator iter = processes.get(i).listProperties(emits);
            //RDFNode emissions = processes.get(i).getProperty(emits).getResource();
            System.out.println("searching emissions for process " + processes.get(i) + "(" + i + ")");
            while (iter.hasNext()) {
                System.out.println("found emissions for process " + processes.get(i) + "(" + i + ")");
                Resource emission = iter.nextStatement().getResource();
                System.out.println(emission);
                Resource nature = emission.getProperty(hasNature).getResource();
                double emissionValue = emission.getProperty(value).getDouble();
                System.out.println("Setting emissionValue " + emissionValue + " for process " + processes.get(i) + "(i = " + i  + ")");
                b.set(i, elementaryFlowNatures.indexOf(nature), emissionValue);
            }
        }
        System.out.println(b);
    }

    public void inverseMatrix () {
        // We will use Gauss-Jordan method for inverting
        MatrixInverter inverter = a.withInverter(LinearAlgebra.GAUSS_JORDAN);
        // The 'b' matrix will be dense
        Matrix b = inverter.inverse(LinearAlgebra.DENSE_FACTORY);

        System.out.println(b);
    }

    public void writeModel(String outputFileName) throws IOException
    {
        FileOutputStream out = new FileOutputStream(outputFileName);
        model.write(out, "RDF/XML-ABBREV");
    }

    public void writeInferredModel(String outputFileName) throws IOException
    {
        FileOutputStream out = new FileOutputStream(outputFileName);
        infModel.write(out, "RDF/XML-ABBREV");
    }

    public void createCumulatedEcologicalFlows()
    {
        Property emits = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "emits");
        Property hasNature = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "hasNature");
        Property value = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "value");
        Property calculateElementaryFlow = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "CalculatedElementaryFlow");

        System.out.println("AnonId = " + AnonId.create());
        for (int i = 0; i < processes.size(); i++) {
            for (int j = 0; j < elementaryFlowNatures.size(); j++) {
                System.out.println("i = " + i + " j = " + j + " process = " + processes.get(i) + " value = " + d.get(i,  j));
                processes.get(i).addProperty(emits,
                    model.createResource("http://www.myc-sense.com/ontologies/bc#" + AnonId.create().toString())
                    .addProperty(hasNature, elementaryFlowNatures.get(j))
                    .addProperty(value, model.createTypedLiteral((float) d.get(i, j)))
                    .addProperty(RDF.type, calculateElementaryFlow));                
            }
        }
    }

    public void extractElementaryNatures()
    {
        Resource elementaryFlowNature = ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#ElementaryFlowNature");
        Selector selector = new SimpleSelector(null, RDF.type, (RDFNode) elementaryFlowNature);
        StmtIterator iter = model.listStatements( selector );

        elementaryFlowNatures = new ArrayList<Resource>();
        if (iter.hasNext()) {
            System.out.println("found some elementary flow nature");
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                elementaryFlowNatures.add(s.getSubject());
            }
        }

        System.out.println(elementaryFlowNatures);
    }

    public ArrayList<Resource> findRelations(Resource process) {
        /** 
         * return the relations for a given process
        **/
        System.out.println("finding relations");
        Property hasDestination = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "hasDestination");
        Property hasOrigin = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "hasOrigin");

        Selector selector = new SimpleSelector(null, hasOrigin, process);
        StmtIterator iter = model.listStatements( selector );

        ArrayList<Resource> relations = new ArrayList<Resource>();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                //Resource r = iter.nextResource();
                Statement s = iter.nextStatement();
                relations.add(s.getSubject());
            }
            System.out.println("relations = " + relations);
            return relations;
        }
        else {
            return new ArrayList<Resource>();
        }
    }

    public Double getCoefficient(Resource relation) {
        Property hasWeight = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "hasWeight");
        Property value = ResourceFactory.createProperty("http://www.myc-sense.com/ontologies/bc#", "value");
        Resource coefficient = relation.getProperty(hasWeight).getResource();

        return coefficient.getProperty(value).getDouble();
    }
}