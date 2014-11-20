package com.mycsense.carbondb; 

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.RDFNode;

import com.mycsense.carbondb.architecture.*;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.Process;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.la4j.inversion.MatrixInverter;
import org.la4j.LinearAlgebra;

import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reasoner {

    protected Model model;
    protected InfModel infModel;
    protected RelationRepo relationRepo;
    protected SingleElementRepo singleElementRepo;
    protected ReferenceRepo referenceRepo;

    protected com.hp.hpl.jena.reasoner.Reasoner jenaReasoner;
    protected Matrix dependencyMatrix, transitiveDependencyMatrix;
    protected Matrix uncertaintyMatrix, transitiveUncertaintyMatrix;
    protected Matrix ecologicalMatrix, cumulativeEcologicalMatrix;
    protected Matrix flowToImpactsMatrix, flowToImpactsUncertaintyMatrix;
    protected Matrix impactMatrix;
    protected Matrix ecologicalUncertaintyMatrix, cumulativeEcologicalUncertaintyMatrix;
    protected ArrayList<Resource> elementaryFlowTypes, processes, impactTypes;
    protected Double threshold = new Double(0.1);
    public ReasonnerReport report = new ReasonnerReport();
    protected UnitsRepo unitsRepo;
    protected GroupRepo groupRepo;

    private final Logger log = LoggerFactory.getLogger(Reasoner.class);

    public Reasoner (Model model, UnitsRepo unitsRepo) {
        this.model = model;
        this.unitsRepo = unitsRepo;
        jenaReasoner = PelletReasonerFactory.theInstance().create();
    }

    public void run () {
        /*
        load the ontology -> ontologyloader
        convert source relations -> sourceRelations convert
        calculate ecological flows -> calculate ecological flows
        */
        log.info("Begin reasonning");
        infModel = ModelFactory.createInfModel( jenaReasoner, model );
        ((PelletInfGraph) infModel.getGraph()).classify();
        ((PelletInfGraph) infModel.getGraph()).realize();

        RepoFactory.clear();
        RepoFactory.setModel(infModel);
        //RepoFactory.setUnitsRepo(unitsRepo);
        RepoFactory.setReasonnerReport(report);
        relationRepo = RepoFactory.getRelationRepo();
        singleElementRepo = RepoFactory.getSingleElementRepo();
        referenceRepo = RepoFactory.getReferenceRepo();
        groupRepo = RepoFactory.getGroupRepo();
        log.info("Loading and translating sourceRelations");

        // We load the ontology
        CarbonOntology ontology = CarbonOntology.getInstance();
        ontology.setElementaryFlowTypesTree(RepoFactory.getTypeRepo().getElementaryFlowTypesTree());
        ontology.setImpactTypesTree(RepoFactory.getTypeRepo().getImpactTypesTree());
        ontology.setReferences(referenceRepo.getReferences());
        ontology.setProcessGroups(groupRepo.getProcessGroups());
        ontology.setCoefficientGroups(groupRepo.getCoefficientGroups());
        for (Process process: singleElementRepo.getProcesses()) {
            try {
                ontology.addProcess(process);
            }
            catch (AlreadyExistsException e) {
                log.warn(e.getMessage());
            }
        }
        for (Coefficient coefficient: singleElementRepo.getCoefficients()) {
            try {
                ontology.addCoefficient(coefficient);
            }
            catch (AlreadyExistsException e) {
                log.warn(e.getMessage());
            }
        }
        ontology.setSourceRelations(RepoFactory.getRelationRepo().getSourceRelations());

        // and we process the ontology only using the object model
        for (SourceRelation sourceRelation: ontology.getSourceRelations().values()) {
            ArrayList<SourceRelation.TranslationDerivative> derivatives = new ArrayList<>();
            try {
                derivatives = sourceRelation.translate();
            } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
                log.warn(e.getMessage());
            }
            for (SourceRelation.TranslationDerivative derivative: derivatives) {
                try {
                    derivative.transformToDerivedRelation();
                } catch (NoElementFoundException e) {
                    log.warn(e.getMessage());
                }
            }
        }

        log.info("Creating ecological matrices");
        createEcologicalMatrix();
        createFlowToImpactsMatrix();

        // version with uncertainty calculation
        //createMatrices();
        //iterativeCalculation();
        //ecologicalCumulatedFlowCalculation();

        // matrix inversion method
        //createMatrix();
        //calculateCumulatedEcologicalFlows();

        log.info("Creating matrix");
        createMatrices();
        log.info("Calculating cumulative flows");
        iterativeCalculationWithoutUncertainties();
        cumulativeEcologicalMatrix = transitiveDependencyMatrix.multiply(ecologicalMatrix);
        log.info("Calculating impacts");
        impactMatrix = cumulativeEcologicalMatrix.multiply(flowToImpactsMatrix.transpose());

        log.info("Creating calculated flows");
        createCumulatedEcologicalFlows();
        log.info("Creating impacts");
        createImpacts();

        log.info("Reasoning finished");
    }

    public void createMatrix() {
        dependencyMatrix = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < processes.size(); i++) {
            dependencyMatrix.set(i, i, 1.0);
            ArrayList<Resource> relations = relationRepo.getRelationsForProcess(processes.get(i));
            for (Resource relation : relations) {
                RDFNode downStreamProcess = relation.getProperty(Datatype.hasDestinationProcess).getResource();
                double value = dependencyMatrix.get(processes.indexOf(downStreamProcess), i);
                dependencyMatrix.set(
                    processes.indexOf(downStreamProcess),
                    i,
                    value-relationRepo.getCoefficientValueForRelation(relation)
                );
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
        cumulativeEcologicalUncertaintyMatrix = new CCSMatrix(processes.size(), elementaryFlowTypes.size());
        cumulativeEcologicalMatrix = new CCSMatrix(processes.size(), elementaryFlowTypes.size());


        for (int j = 0; j < elementaryFlowTypes.size(); j++) {

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
        HashSet<Process> processes = CarbonOntology.getInstance().getProcesses();

        // @todo transform the processes hashset into an arraylist to keep the following logic intact
        dependencyMatrix = new CCSMatrix(processes.size(), processes.size());
        uncertaintyMatrix = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < processes.size(); i++) {
            // @todo get the relations from the Process directly
            ArrayList<Resource> relations = relationRepo.getRelationsForProcess(processes.get(i));
            for (Resource relation : relations) {
                RDFNode downStreamProcess = relation.getProperty(Datatype.hasDestinationProcess).getResource();
                double value = dependencyMatrix.get(processes.indexOf(downStreamProcess), i);
                double uncertainty = uncertaintyMatrix.get(processes.indexOf(downStreamProcess), i);
                dependencyMatrix.set(
                    processes.indexOf(downStreamProcess),
                    i,
                    value+relationRepo.getCoefficientValueForRelation(relation)
                );
                // @todo: check if the uncertainties should be added
                uncertaintyMatrix.set(
                    processes.indexOf(downStreamProcess),
                    i,
                    uncertainty+relationRepo.getCoefficientUncertaintyForRelation(relation)
                );
            }
        }
    }

    protected void createEcologicalMatrix() {
        HashSet<Process> processes = CarbonOntology.getInstance().getProcesses();
        Collection<ElementaryFlowType> elementaryFlowTypes = CarbonOntology.getInstance().getElementaryFlowTypes().values();
        ecologicalMatrix = new CCSMatrix(processes.size(), elementaryFlowTypes.size());
        ecologicalUncertaintyMatrix = new CCSMatrix(processes.size(), elementaryFlowTypes.size());

        int i = 0;
        for (Process process: processes) {
            i++;
            HashMap<String, ElementaryFlow> flows = process.getFlows();
            int j = 0;
            for (ElementaryFlowType type: elementaryFlowTypes) {
                j++;
                if (flows.containsKey(type.getId())) {
                    ecologicalMatrix.set(i, j, flows.get(type.getId()).getValue().value);
                    ecologicalUncertaintyMatrix.set(i, j, flows.get(type.getId()).getValue().uncertainty);
                }
                else {
                    ecologicalMatrix.set(i, j, 0.0);
                    ecologicalUncertaintyMatrix.set(i, j, 0.0);
                }
            }
        }
    }

    protected void createFlowToImpactsMatrix() {
        // W -> cols = EFT, rows = IT
        Collection<ElementaryFlowType> elementaryFlowTypes = CarbonOntology.getInstance().getElementaryFlowTypes().values();
        Collection<ImpactType> impactTypes = CarbonOntology.getInstance().getImpactTypes().values();

        flowToImpactsMatrix = new CCSMatrix(impactTypes.size(), elementaryFlowTypes.size());
        flowToImpactsUncertaintyMatrix = new CCSMatrix(impactTypes.size(), elementaryFlowTypes.size());

        int i = 0;
        for (ImpactType impactType: impactTypes) {
            i++;
            HashMap<ElementaryFlowType, Value> components = impactType.getComponents();
            int j = 0;
            for (ElementaryFlowType flowType: elementaryFlowTypes) {
                j++;
                if (components.containsKey(flowType)) {
                    flowToImpactsMatrix.set(i, j, components.get(flowType).value);
                    flowToImpactsUncertaintyMatrix.set(i, j, components.get(flowType).uncertainty);
                }
                else {
                    flowToImpactsMatrix.set(i, j, 0.0);
                    flowToImpactsUncertaintyMatrix.set(i, j, 0.0);
                }
            }
        }
    }

    protected void createCumulatedEcologicalFlows()
    {
        for (int i = 0; i < processes.size(); i++) {
            for (int j = 0; j < elementaryFlowTypes.size(); j++) {
                double value = cumulativeEcologicalMatrix.get(i, j);
                if (value != 0.0) {
                    String unitID = singleElementRepo.getUnit(processes.get(i));
                    if (!unitID.equals("")) {
                        value *= unitsRepo.getConversionFactor(unitID);
                    }

                    singleElementRepo.addCumulatedEcologicalFlow(
                            processes.get(i),
                            elementaryFlowTypes.get(j),
                            value,
                            //cumulativeEcologicalUncertaintyMatrix.get(i,j)
                            0.0
                    );
                }
            }
        }
    }

    protected void createImpacts()
    {
        for (int i = 0; i < processes.size(); i++) {
            for (int j = 0; j < impactTypes.size(); j++) {
                double value = impactMatrix.get(i, j);
                if (value != 0.0) {
                    String unitID = singleElementRepo.getUnit(processes.get(i));
                    if (!unitID.equals("")) {
                        value *= unitsRepo.getConversionFactor(unitID);
                    }

                    singleElementRepo.addImpact(
                            processes.get(i),
                            impactTypes.get(j),
                            value,
                            //cumulativeEcologicalUncertaintyMatrix.get(i,j)
                            0.0
                    );
                }
            }
        }
    }

    protected ArrayList<DerivedRelation> createDerivedRelations(ArrayList<DerivedRelation> derivedRelations, Resource sourceRelation)
        throws IllegalArgumentException
    {
        Resource coeff = null, sourceProcess = null, destinationProcess = null;
        ArrayList<DerivedRelation> derivedRelationsToRemove = new ArrayList<>();
        for (DerivedRelation derivedRelation : derivedRelations) {
            try  {
                coeff = singleElementRepo.getCoefficientForDimension(derivedRelation.coeff, derivedRelation.coeffUnit.getURI());
            }
            catch (NoElementFoundException e) {
                coeff = null;
                derivedRelationsToRemove.add(derivedRelation);
                // having a null coefficient is a common use case
            }
            catch (MultipleElementsFoundException e) {
                //@todo fix this (coeff is null at this point)
                report.addWarning(e.getMessage());
            }
            if (coeff != null) {
                try  {
                    sourceProcess = singleElementRepo.getProcessForDimension(derivedRelation.source, derivedRelation.sourceUnit.getURI());
                }
                catch (NoElementFoundException e) {
                    sourceProcess = singleElementRepo.createProcess(derivedRelation.source, derivedRelation.sourceUnit.getURI());
                }
                catch (MultipleElementsFoundException e) {
                    report.addWarning(e.getMessage());
                }
                try  {
                    destinationProcess = singleElementRepo.getProcessForDimension(derivedRelation.destination, derivedRelation.destinationUnit.getURI());
                }
                catch (NoElementFoundException e) {
                    destinationProcess = singleElementRepo.createProcess(derivedRelation.destination, derivedRelation.destinationUnit.getURI());
                }
                catch (MultipleElementsFoundException e) {
                    report.addWarning(e.getMessage());
                }
                if (null != sourceProcess && null != coeff && null != destinationProcess) {
                    relationRepo.addDerivedRelation(sourceProcess, coeff, destinationProcess, derivedRelation.exponent, sourceRelation);
                    derivedRelation.sourceURI = sourceProcess.getURI();
                    derivedRelation.coeffURI = coeff.getURI();
                    derivedRelation.destinationURI = destinationProcess.getURI();
                }
            }
        }
        for (DerivedRelation derivedRelation: derivedRelationsToRemove) {
            derivedRelations.remove(derivedRelation);
        }
        return derivedRelations;
    }

    public InfModel getInfModel() {
        return infModel;
    }

    public Model getModel() {
        return model;
    }
}