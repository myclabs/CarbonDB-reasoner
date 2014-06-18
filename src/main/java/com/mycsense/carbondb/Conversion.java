package com.mycsense.carbondb; 

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import java.io.*;
import java.io.FileOutputStream;
import com.hp.hpl.jena.util.FileManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.reasoner.*;
import java.lang.StringBuilder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Conversion {

/**
TODO: transform this class to remove the dependency to jena (add a class that will load the model)
      Resource -> Keyword, move the creation of ontology elements in another class
      this class should only use a class Model containing groups, microRelations and macroRelations
**/

    //protected Model model;
    public Model model;
    protected InfModel infModel;
    protected ArrayList<Resource> processes;
    protected ArrayList<Resource> elementaryFlowNatures;

    Conversion (Model model) {
        this.model = model;
    }

    public void run() {
        for (MacroRelation macroRelation: getMacroRelations()) {
            createMicroRelations(macroRelation.translate());
        }
    }

    public ArrayList<MacroRelation> getMacroRelations() {
        ArrayList<MacroRelation> macroRelations = new ArrayList<MacroRelation>();

        Selector selector = new SimpleSelector(null, RDF.type, Datatype.Relation);
        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.Relation);
        while (i.hasNext()) {
            Resource macroRelation = i.next();
            macroRelations.add(new MacroRelation(
                getGroup(macroRelation.getProperty(Datatype.hasOrigin).getResource()),
                getGroup(macroRelation.getProperty(Datatype.hasWeight).getResource()),
                getGroup(macroRelation.getProperty(Datatype.hasDestination).getResource())
            ));
        }

        return macroRelations;
    }

    Conversion (String inputFileName) {

        model = ModelFactory.createDefaultModel();

        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException(
                        "File: " + inputFileName + " not found");
        }

        model.read(in, null);

        // Those lines shall be converted to an integration test (+ translate)
        Dimension dimA1 = new Dimension();
        dimA1.add(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#kw1"));
        dimA1.add(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#kw2"));
        System.out.println("+++ RDF file readed +++");
        System.out.println(getGroupDimSet(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")));
        System.out.println("+++ group fp1 +++");
        System.out.println(getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")));
        System.out.println("+++ group fc +++");
        System.out.println(getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fc")));
        System.out.println(getProcessForDimension(dimA1));

        /*try {
            FileOutputStream out = new FileOutputStream("ontologies/bc-test-inferred-java.rdf");
            model.write(out);
        }
        catch (IOException e) {
            System.out.println("error while writing the onto file");
        }*/
    }

    protected Resource getProcessForDimension(Dimension dimension)
    {
        return getElementForDimension(dimension, "process");
    }

    protected Resource getCoefficientForDimension(Dimension dimension)
    {
        return getElementForDimension(dimension, "coefficient");
    }

    protected Resource getElementForDimension(Dimension dimension, String type)
    {
        // @todo: throw an exception instead of returning null when the element could not be found?
        System.out.println("--- Process exists called ---");
        Resource single;
        if (type.equals("process")) {
            single = Datatype.SingleProcess;
        }
        else {
            single = Datatype.SingleCoefficient;
        }
        if (dimension.size() == 0) {
            return (Resource) null;
        }
        Iterator<Resource> iter = dimension.keywords.iterator();
        Resource keyword = iter.next();
        Selector selector = new SimpleSelector(null, Datatype.hasKeyword, keyword);
        ResIterator statementIter = model.listSubjectsWithProperty(Datatype.hasKeyword, keyword);
        //StmtIterator statementIter = model.listStatements( selector );
        List<Resource> candidates = statementIter.toList();
        System.out.println(candidates);
        Iterator<Resource> i = candidates.iterator();
        while (i.hasNext()) {
            Resource candidate = i.next();
            if (!model.contains(candidate, RDF.type, (RDFNode) single)) {
                i.remove();
            }
            else {
                NodeIterator nodeIter = model.listObjectsOfProperty(candidate, Datatype.hasKeyword);
                int listSize = nodeIter.toList().size();
                if (listSize != dimension.size()) {
                    System.out.println("removing candidate (" + candidate + "), kw size = " + listSize + " / dim size = " + dimension.size());
                    i.remove();
                }
            }
        }
        System.out.println(candidates);

        while (iter.hasNext()) {
            keyword = iter.next();
            i = candidates.iterator();
            while (i.hasNext()) {
                Resource candidate = i.next();
                if (!model.contains(candidate, Datatype.hasKeyword, (RDFNode) keyword)) {
                    i.remove();
                }
            }
        }
        System.out.println(candidates);
        if (candidates.isEmpty()) {
            return (Resource) null;
        }
        return candidates.get(0);
    }

    protected Group getGroup(Resource family)
    {
        Group group = new Group(getGroupDimSet(family));
        return group;
    }

    protected DimensionSet getGroupDimSet(Resource family)
    {
        Selector selector = new SimpleSelector(family, Datatype.hasDimension, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        DimensionSet dimSet = new DimensionSet();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                dimSet.add(getDimensionKeywords((Resource) s.getObject()));
            }
        }
        return dimSet;
    }

    protected Dimension getDimensionKeywords(Resource dimension)
    {
        Selector selector = new SimpleSelector(dimension, Datatype.hasKeyword, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        Dimension dim = new Dimension();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                dim.add((Resource) s.getObject());
            }
        }
        return dim;
    }

    public void createMicroRelations(ArrayList<MicroRelation> microRelations)
    {
        for (MicroRelation microRelation: microRelations) {
            Resource sourceProcess = getProcessForDimension(microRelation.source);
            Resource coeff = getCoefficientForDimension(microRelation.coeff);
            Resource destinationProcess = getProcessForDimension(microRelation.destination);
            if (sourceProcess != null && coeff != null && destinationProcess != null) {
                System.out.println("Creating micro-relation");
                sourceProcess.addProperty(Datatype.hasDetailedRelation,
                    model.createResource(Datatype.getURI() + AnonId.create().toString())
                    .addProperty(RDF.type, Datatype.Relation)
                    .addProperty(Datatype.hasOrigin, sourceProcess)
                    .addProperty(Datatype.hasWeight, coeff)
                    .addProperty(Datatype.hasDestination, destinationProcess));
            }
        }
    }
}