package com.mycsense.carbondb; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.RDFNode;

import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.matrix.Matrix;
import org.la4j.inversion.MatrixInverter;
import org.la4j.LinearAlgebra;

import org.mindswap.pellet.jena.PelletReasonerFactory;

public class Reasoner {

    protected Model model;
    protected InfModel infModel;
    protected Reader reader;
    protected Writer writer;
    protected com.hp.hpl.jena.reasoner.Reasoner jenaReasoner;
    protected Matrix a, b, c, d;
    protected ArrayList<Resource> elementaryFlowNatures, processes;

    Reasoner (Model model) {
        this.model = model;
        jenaReasoner = PelletReasonerFactory.theInstance().create();
    }

    public void run () {
        /*
        load the ontology -> ontologyloader
        convert macro relations -> macrorelations convert
        calculate ecological flows -> calculate ecological flows
        */
        infModel = ModelFactory.createInfModel( jenaReasoner, model );
        reader = new Reader(infModel);
        writer = new Writer(infModel);
        for (MacroRelation macroRelation: reader.getMacroRelations()) {
            createMicroRelations(macroRelation.translate());
        }

        processes = reader.getSingleProcesses();
        elementaryFlowNatures = reader.getElementaryFlowNatures();

        createMatrix();
        inverseMatrix();
        createEcologicalMatrix();
        calculateCumulatedEcologicalFlows();
        createCumulatedEcologicalFlows();
    }

    protected void calculateCumulatedEcologicalFlows()
    {
        // We will use Gauss-Jordan method for inverting
        MatrixInverter inverter = a.withInverter(LinearAlgebra.GAUSS_JORDAN);
        // The 'b' matrix will be dense
        c = inverter.inverse(LinearAlgebra.DENSE_FACTORY);

        d = c.multiply(b);
    }

    protected void createMatrix() {
        a = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < processes.size(); i++) {
            a.set(i, i, 1.0);
            ArrayList<Resource> relations = reader.getRelationsForProcess(processes.get(i));
            for (Resource relation : relations) {
                RDFNode downStreamProcess = relation.getProperty(Datatype.hasDestination).getResource();
                a.set(processes.indexOf(downStreamProcess), i, - reader.getCoefficientValueForRelation(relation));
            }
        }
    }

    protected void createEcologicalMatrix() {
        b = new CCSMatrix(processes.size(), elementaryFlowNatures.size());

        for (int i = 0; i < processes.size(); i++) {
            HashMap<Resource, Double> emissions = reader.getEmissionsForProcess(processes.get(i));
            for (Entry<Resource, Double> e : emissions.entrySet()) {
                Resource nature = e.getKey();
                Double value = e.getValue();
                b.set(i, elementaryFlowNatures.indexOf(e.getKey()), e.getValue());
            }
        }
    }

    protected void inverseMatrix () {
        // We will use Gauss-Jordan method for inverting
        MatrixInverter inverter = a.withInverter(LinearAlgebra.GAUSS_JORDAN);
        // The 'b' matrix will be dense
        b = inverter.inverse(LinearAlgebra.DENSE_FACTORY);
    }

    protected void createCumulatedEcologicalFlows()
    {
        for (int i = 0; i < processes.size(); i++) {
            for (int j = 0; j < elementaryFlowNatures.size(); j++) {
                writer.addCumulatedEcologicalFlow(processes.get(i), elementaryFlowNatures.get(j), (float) d.get(i, j));
            }
        }
    }

    protected void createMicroRelations(ArrayList<MicroRelation> microRelations)
    {
        for (MicroRelation microRelation: microRelations) {
            Resource sourceProcess = reader.getElementForDimension(microRelation.source, Datatype.SingleProcess);
            Resource coeff = reader.getElementForDimension(microRelation.coeff, Datatype.SingleCoefficient);
            Resource destinationProcess = reader.getElementForDimension(microRelation.destination, Datatype.SingleProcess);
            if (sourceProcess != null && coeff != null && destinationProcess != null) {
                writer.addMicroRelation(sourceProcess, coeff, destinationProcess);
            }
        }
    }

    public InfModel getInfModel() {
        return infModel;
    }

    public Model getModel() {
        return model;
    }
}