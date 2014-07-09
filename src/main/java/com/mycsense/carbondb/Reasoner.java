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
import org.la4j.vector.Vector;
import org.la4j.inversion.MatrixInverter;
import org.la4j.LinearAlgebra;

import org.mindswap.pellet.jena.PelletReasonerFactory;

public class Reasoner {

    protected Model model;
    protected InfModel infModel;
    protected Reader reader;
    protected Writer writer;
    protected com.hp.hpl.jena.reasoner.Reasoner jenaReasoner;
    protected Matrix dependencyMatrix, transitiveDependencyMatrix;
    protected Matrix uncertaintyMatrix, transitiveUncertaintyMatrix;
    protected Matrix ecologicalMatrix, cumulativeEcologicalMatrix;
    protected Matrix ecologicalUncertaintyMatrix, cumulativeEcologicalUncertaintyMatrix;
    protected ArrayList<Resource> elementaryFlowNatures, processes;
    protected Double threshold = new Double(0.1);

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

        createEcologicalMatrix();
        createMatrices();
        iterativeCalculation();
        ecologicalCumulatedFlowCalculation();
        createCumulatedEcologicalFlows();
    }

    protected void iterativeCalculation()
    {
        Matrix prevTransitiveDependencySum, transitiveDependencySum,
               prevUncertaintySum, uncertaintySum,
               prevDependencyProduct, dependencyProduct,
               prevUncertaintyProduct, uncertaintyProduct;

        // R^0
        transitiveDependencySum = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < transitiveDependencySum.rows(); i++)
            transitiveDependencySum.set(i, i, 1.0);

        // R^0 + R^1
        prevTransitiveDependencySum = transitiveDependencySum.copy();
        transitiveDependencySum = transitiveDependencySum.add(dependencyMatrix);
        dependencyProduct = dependencyMatrix;

        uncertaintySum = uncertaintyMatrix;
        uncertaintyProduct = uncertaintySum;

        int maxIter = 0;
        while (differenceGreaterThanThreshold(prevTransitiveDependencySum, transitiveDependencySum) && maxIter < 100) {
            // value
            prevTransitiveDependencySum = transitiveDependencySum.copy();

            dependencyProduct = dependencyProduct.multiply(dependencyMatrix);
            transitiveDependencySum = transitiveDependencySum.add(dependencyProduct);

            uncertaintyProduct = matrixProductUncertainty(uncertaintyProduct, uncertaintyMatrix);
            uncertaintySum = matrixSumUncertainty(prevTransitiveDependencySum, uncertaintySum, dependencyProduct, uncertaintyProduct);

            maxIter++;
        }
        transitiveUncertaintyMatrix = uncertaintySum;
        transitiveDependencyMatrix = transitiveDependencySum;
    }


    protected void ecologicalCumulatedFlowCalculation()
    {
        cumulativeEcologicalUncertaintyMatrix = new CCSMatrix(processes.size(), elementaryFlowNatures.size());
        cumulativeEcologicalMatrix = new CCSMatrix(processes.size(), elementaryFlowNatures.size());


        for (int j = 0; j < elementaryFlowNatures.size(); j++) {

            Vector column = ecologicalMatrix.getColumn(j);
            Vector uncertaintyColumn = ecologicalUncertaintyMatrix.getColumn(j);

            for (int i = 0; i < transitiveDependencyMatrix.rows(); i++) {

                double acc = 0.0;
                double accUncertainty = 0.0;

                for (int k = 0; k < transitiveDependencyMatrix.columns(); k++) {
                    double emission = transitiveDependencyMatrix.get(i, k) * column.get(k);
                    double emissionUncertainty = productUncertainty(transitiveUncertaintyMatrix.get(i, k), uncertaintyColumn.get(k));
                    accUncertainty = sumUncertainty(acc, accUncertainty,
                                                    emission, emissionUncertainty);
                    acc += emission;
                }
                cumulativeEcologicalUncertaintyMatrix.set(i, j, accUncertainty);
                cumulativeEcologicalMatrix.set(i, j, acc);
            }
        }
    }

    protected Matrix matrixProductUncertainty(Matrix u1, Matrix u2)
    {
        Matrix r = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < u1.rows(); i++) {
            for (int j = 0; j < u2.rows(); j++) {
                r.set(i, j, productUncertainty(u1.get(i,j), u2.get(i,j)));
            }
        }
        return r;
    }

    protected double productUncertainty(double u1, double u2)
    {
        return Math.sqrt(Math.pow(u1, 2) + Math.pow(u2, 2));
    }

    protected Matrix matrixSumUncertainty(Matrix v1, Matrix u1, Matrix v2, Matrix u2)
    {
        Matrix r = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < u1.rows(); i++) {
            for (int j = 0; j < u1.columns(); j++) {
                r.set(i,j,sumUncertainty(v1.get(i,j), u1.get(i,j), v2.get(i,j), u2.get(i,j)));
            }
        }
        return r;
    }

    protected double sumUncertainty(double v1, double u1, double v2, double u2)
    {
        if (Math.abs(v1 + v2) == 0) {
            return 0.0;
        }
        else {
            return Math.sqrt(Math.pow(v1 * u1, 2) + Math.pow(v2 * u2, 2)) / Math.abs(v1 + v2);
        }
    }

    protected Matrix sqrtMatrix(Matrix m)
    {
        Matrix r = m.copy();
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.columns(); j++) {
                r.set(i, j, Math.sqrt(m.get(i,j)));
            }
        }
        return r;
    }

    protected boolean differenceGreaterThanThreshold(Matrix a, Matrix b)
    {
        Matrix c = a.subtract(b);
        for (int i = 0; i < c.rows(); i++) {
            for (int j = 0; j < c.columns(); j++) {
                if (Math.abs(c.get(i,j)) >= threshold) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void createMatrices() {
        dependencyMatrix = new CCSMatrix(processes.size(), processes.size());
        uncertaintyMatrix = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < processes.size(); i++) {
            ArrayList<Resource> relations = reader.getRelationsForProcess(processes.get(i));
            for (Resource relation : relations) {
                RDFNode downStreamProcess = relation.getProperty(Datatype.hasDestination).getResource();
                dependencyMatrix.set(processes.indexOf(downStreamProcess), i, reader.getCoefficientValueForRelation(relation));
                uncertaintyMatrix.set(processes.indexOf(downStreamProcess), i, reader.getCoefficientUncertaintyForRelation(relation));
            }
        }
    }

    protected void createEcologicalMatrix() {
        ecologicalMatrix = new CCSMatrix(processes.size(), elementaryFlowNatures.size());
        ecologicalUncertaintyMatrix = new CCSMatrix(processes.size(), elementaryFlowNatures.size());

        for (int i = 0; i < processes.size(); i++) {
            HashMap<Resource, Value> emissions = reader.getEmissionsForProcess(processes.get(i));
            for (Entry<Resource, Value> e : emissions.entrySet()) {
                ecologicalMatrix.set(i, elementaryFlowNatures.indexOf(e.getKey()), e.getValue().value);
                ecologicalUncertaintyMatrix.set(i, elementaryFlowNatures.indexOf(e.getKey()), e.getValue().uncertainty);
            }
        }
    }

    protected void createCumulatedEcologicalFlows()
    {
        for (int i = 0; i < processes.size(); i++) {
            for (int j = 0; j < elementaryFlowNatures.size(); j++) {
                writer.addCumulatedEcologicalFlow(processes.get(i),
                                                  elementaryFlowNatures.get(j),
                                                  cumulativeEcologicalMatrix.get(i, j),
                                                  cumulativeEcologicalUncertaintyMatrix.get(i,j)
                                                 );
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