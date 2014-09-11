package com.mycsense.carbondb; 

import java.util.ArrayList;
import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.RDFNode;

import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.la4j.inversion.MatrixInverter;
import org.la4j.LinearAlgebra;

import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.mindswap.pellet.jena.PelletInfGraph;

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
    public ReasonnerReport report = new ReasonnerReport();
    protected UnitsRepo unitsRepo;

    public Reasoner (Model model, UnitsRepo unitsRepo) {
        this.model = model;
        this.unitsRepo = unitsRepo;
        jenaReasoner = PelletReasonerFactory.theInstance().create();
    }

    public void run () {
        /*
        load the ontology -> ontologyloader
        convert macro relations -> macrorelations convert
        calculate ecological flows -> calculate ecological flows
        */
        System.out.println("beginning reasonning");
        infModel = ModelFactory.createInfModel( jenaReasoner, model );
        ((PelletInfGraph) infModel.getGraph()).classify();
        ((PelletInfGraph) infModel.getGraph()).realize();

        reader = new Reader(infModel, unitsRepo);
        writer = new Writer(infModel);
        System.out.println("loading and translating macroRelations");
        for (Resource macroRelationResource: reader.getMacroRelationsResources()) {
            try {
                MacroRelation macroRelation = reader.getMacroRelation(macroRelationResource);
                createMicroRelations(macroRelation.translate());
            }
            catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
                report.addError(e.getMessage());
            }
        }

        System.out.println("getting single processes");
        processes = reader.getSingleProcesses();
        System.out.println("getting elementary flows");
        elementaryFlowNatures = reader.getElementaryFlowNatures();

        System.out.println("creating ecological matrix");
        createEcologicalMatrix();

        // version with uncertainty calculation
        //createMatrices();
        //iterativeCalculation();
        //ecologicalCumulatedFlowCalculation();

        // matrix inversion method
        //createMatrix();
        //calculateCumulatedEcologicalFlows();

        System.out.println("creating matrix");
        createMatrices();
        System.out.println("calculating cumulative flows");
        iterativeCalculationWithoutUncertainties();
        cumulativeEcologicalMatrix = transitiveDependencyMatrix.multiply(ecologicalMatrix);

        System.out.println("creating calculated flows");
        createCumulatedEcologicalFlows();
        System.out.println("reasoning finished");
    }

    public void createMatrix() {
        dependencyMatrix = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < processes.size(); i++) {
            dependencyMatrix.set(i, i, 1.0);
            ArrayList<Resource> relations = reader.getRelationsForProcess(processes.get(i));
            for (Resource relation : relations) {
                RDFNode downStreamProcess = relation.getProperty(Datatype.hasDestination).getResource();
                double value = dependencyMatrix.get(processes.indexOf(downStreamProcess), i);
                dependencyMatrix.set(processes.indexOf(downStreamProcess), i, value-reader.getCoefficientValueForRelation(relation));
            }
        }
    }

    public void calculateCumulatedEcologicalFlows()
    {
        MatrixInverter inverter = dependencyMatrix.withInverter(LinearAlgebra.GAUSS_JORDAN);
        transitiveDependencyMatrix = inverter.inverse(LinearAlgebra.SPARSE_FACTORY);
        cumulativeEcologicalMatrix = transitiveDependencyMatrix.multiply(ecologicalMatrix);
    }

    protected void iterativeCalculationWithoutUncertainties()
    {
        Matrix dependencyProduct, prevTransitiveDependencyMatrix;

        // R^0
        transitiveDependencyMatrix = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < transitiveDependencyMatrix.rows(); i++)
            transitiveDependencyMatrix.set(i, i, 1.0);

        // R^0 + R^1
        prevTransitiveDependencyMatrix = transitiveDependencyMatrix.copy();
        transitiveDependencyMatrix = transitiveDependencyMatrix.add(dependencyMatrix);
        dependencyProduct = dependencyMatrix;

        int maxIter = 0;
        while (differenceGreaterThanThreshold(prevTransitiveDependencyMatrix, transitiveDependencyMatrix) && maxIter < 100) {
            prevTransitiveDependencyMatrix = transitiveDependencyMatrix.copy();
            // R^n-1 + R^n
            dependencyProduct = dependencyProduct.multiply(dependencyMatrix);
            transitiveDependencyMatrix = transitiveDependencyMatrix.add(dependencyProduct);

            maxIter++;
        }
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
        if (Math.pow(u1, 2) + Math.pow(u2, 2) == 0.0)
            return 0.0;
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
        if (Math.abs(v1 + v2) == 0)
            return 0.0;
        else if (Math.pow(v1 * u1, 2) + Math.pow(v2 * u2, 2) == 0.0)
            return 0.0;
        return Math.sqrt(Math.pow(v1 * u1, 2) + Math.pow(v2 * u2, 2)) / Math.abs(v1 + v2);
    }

    protected Matrix sqrtMatrix(Matrix m)
    {
        Matrix r = m.copy();
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.columns(); j++) {
                r.set(i, j, m.get(i,j) == 0.0 ? 0.0 : Math.sqrt(m.get(i,j)));
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
                double value = dependencyMatrix.get(processes.indexOf(downStreamProcess), i);
                double uncertainty = uncertaintyMatrix.get(processes.indexOf(downStreamProcess), i);
                dependencyMatrix.set(processes.indexOf(downStreamProcess), i, value+reader.getCoefficientValueForRelation(relation));
                // @todo: check if the uncertainties should be added
                uncertaintyMatrix.set(processes.indexOf(downStreamProcess), i, uncertainty+reader.getCoefficientUncertaintyForRelation(relation));
            }
        }
    }

    protected void createEcologicalMatrix() {
        ecologicalMatrix = new CCSMatrix(processes.size(), elementaryFlowNatures.size());
        ecologicalUncertaintyMatrix = new CCSMatrix(processes.size(), elementaryFlowNatures.size());

        for (int i = 0; i < processes.size(); i++) {
            HashMap<Resource, Value> emissions = reader.getEmissionsForProcess(processes.get(i));
            for (int j = 0; j < elementaryFlowNatures.size(); j++) {
                if (emissions.containsKey(elementaryFlowNatures.get(j))) {
                    ecologicalMatrix.set(i, j, emissions.get(elementaryFlowNatures.get(j)).value);
                    ecologicalUncertaintyMatrix.set(i, j, emissions.get(elementaryFlowNatures.get(j)).uncertainty);
                }
                else {
                    ecologicalMatrix.set(i, j, 0.0);
                    ecologicalUncertaintyMatrix.set(i, j, 0.0);
                }
            }
        }
    }

    protected void createCumulatedEcologicalFlows()
    {
        for (int i = 0; i < processes.size(); i++) {
            for (int j = 0; j < elementaryFlowNatures.size(); j++) {
                String unitID = reader.getUnit(processes.get(i));
                double value = cumulativeEcologicalMatrix.get(i, j);
                if (!unitID.equals("")) {
                     value *= unitsRepo.getConversionFactor(unitID);
                }

                writer.addCumulatedEcologicalFlow(processes.get(i),
                                                  elementaryFlowNatures.get(j),
                                                  value,
                                                  //cumulativeEcologicalUncertaintyMatrix.get(i,j)
                                                  0.0
                                                 );
            }
        }
    }

    protected void createMicroRelations(ArrayList<MicroRelation> microRelations)
        throws IllegalArgumentException
    {
        Resource coeff = null, sourceProcess = null, destinationProcess = null;
        for (MicroRelation microRelation: microRelations) {
            try  {
                coeff = reader.getElementForDimension(microRelation.coeff, microRelation.coeffUnit, Datatype.SingleCoefficient);
            }
            catch (NoElementFoundException e) {
                // having a null coefficient is a common use case
            }
            catch (MultipleElementsFoundException e) {
                report.addWarning(e.getMessage());
            }
            if (coeff != null) {
                try  {
                    sourceProcess = reader.getElementForDimension(microRelation.source, microRelation.sourceUnit, Datatype.SingleProcess);
                }
                catch (NoElementFoundException e) {
                    sourceProcess = writer.createProcess(microRelation.source, microRelation.sourceUnit);
                }
                catch (MultipleElementsFoundException e) {
                    report.addWarning(e.getMessage());
                }
                try  {
                    destinationProcess = reader.getElementForDimension(microRelation.destination, microRelation.destinationUnit, Datatype.SingleProcess);
                }
                catch (NoElementFoundException e) {
                    destinationProcess = writer.createProcess(microRelation.destination, microRelation.destinationUnit);
                }
                catch (MultipleElementsFoundException e) {
                    report.addWarning(e.getMessage());
                }
                if (null != sourceProcess && null != coeff && null != destinationProcess) {
                    writer.addMicroRelation(sourceProcess, coeff, destinationProcess, microRelation.exponent);
                }
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