/*
 * Copyright 2014, by Benjamin Bertin and Contributors.
 *
 * This file is part of CarbonDB-reasoner project <http://www.carbondb.org>
 *
 * CarbonDB-reasoner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * CarbonDB-reasoner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CarbonDB-reasoner.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributor(s): -
 *
 */

package com.mycsense.carbondb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.InfModel;

import com.mycsense.carbondb.architecture.RepoFactory;
import com.mycsense.carbondb.domain.CarbonOntology;
import com.mycsense.carbondb.domain.Coefficient;
import com.mycsense.carbondb.domain.DerivedRelation;
import com.mycsense.carbondb.domain.ElementaryFlow;
import com.mycsense.carbondb.domain.ElementaryFlowType;
import com.mycsense.carbondb.domain.Impact;
import com.mycsense.carbondb.domain.ImpactType;
import com.mycsense.carbondb.domain.Process;
import com.mycsense.carbondb.domain.Reference;
import com.mycsense.carbondb.domain.SourceRelation;
import com.mycsense.carbondb.domain.Value;
import com.mycsense.carbondb.domain.elementaryFlow.DataSource;
import com.mycsense.carbondb.domain.relation.TranslationDerivative;

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

    protected com.hp.hpl.jena.reasoner.Reasoner jenaReasoner;
    protected Matrix dependencyMatrix, transitiveDependencyMatrix;
    protected Matrix uncertaintyMatrix, transitiveUncertaintyMatrix;
    protected Matrix ecologicalMatrix, cumulativeEcologicalMatrix;
    protected Matrix flowToImpactsMatrix, flowToImpactsUncertaintyMatrix;
    protected Matrix impactMatrix;
    protected Matrix ecologicalUncertaintyMatrix, cumulativeEcologicalUncertaintyMatrix;
    protected Double threshold = 0.1;

    protected ArrayList<ImpactType> impactTypes;
    protected ArrayList<ElementaryFlowType> elementaryFlowTypes;

    protected ArrayList<Process> processes;
    protected CarbonOntology ontology;

    private final Logger log = LoggerFactory.getLogger(Reasoner.class);

    public Reasoner (Model model) {
        this.model = model;
        jenaReasoner = PelletReasonerFactory.theInstance().create();
    }

    public void run () {
        log.info("Begin reasonning");
        infModel = ModelFactory.createInfModel( jenaReasoner, model );
        ((PelletInfGraph) infModel.getGraph()).classify();
        ((PelletInfGraph) infModel.getGraph()).realize();

        RepoFactory.clear();
        RepoFactory.setModel(infModel);
        log.info("Loading and translating sourceRelations");

        loadOntology();
        // We process the ontology only using the object model
        for (SourceRelation sourceRelation: ontology.getSourceRelations().values()) {
            ArrayList<TranslationDerivative> derivatives = new ArrayList<>();
            try {
                derivatives = sourceRelation.translate();
            } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
                log.warn(e.getMessage());
            }
            for (TranslationDerivative derivative: derivatives) {
                try {
                    ontology.addDerivedRelation(derivative.transformToDerivedRelation());
                } catch (NoElementFoundException e) {
                    //log.warn(e.getMessage()); // we do not want to log this, as it is a normal behavior
                } catch (AlreadyExistsException e) {
                    log.warn(e.getMessage());
                }
            }
        }

        elementaryFlowTypes = new ArrayList<>(CarbonOntology.getInstance().getElementaryFlowTypes().values());
        processes = new ArrayList<>(CarbonOntology.getInstance().getProcesses());
        impactTypes = new ArrayList<>(CarbonOntology.getInstance().getImpactTypes().values());

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
        try {
            createCumulatedEcologicalFlows();
        } catch (AlreadyExistsException e) {
            log.warn(e.getMessage());
        }
        log.info("Creating impacts");
        try {
            createImpacts();
        } catch (AlreadyExistsException e) {
            log.warn(e.getMessage());
        }

        log.info("Reasoning finished");
    }

    protected void loadOntology() {
        // We load the ontology starting from the lowest elements in the dependency tree to the highest ones
        ontology = CarbonOntology.getInstance();
        ontology.setElementaryFlowTypesTree(RepoFactory.getTypeRepo().getElementaryFlowTypesTree());
        ontology.setImpactTypesTree(RepoFactory.getTypeRepo().getImpactTypesTree());
        ontology.setReferences(new HashSet<>(RepoFactory.getReferenceRepo().getReferences().values()));
        ontology.setRelationTypes(RepoFactory.getRelationRepo().getRelationTypes());
        for (Process process: RepoFactory.getSingleElementRepo().getProcesses()) {
            try {
                ontology.addProcess(process);
            }
            catch (AlreadyExistsException e) {
                log.warn(e.getMessage());
            }
        }
        for (Coefficient coefficient: RepoFactory.getSingleElementRepo().getCoefficients()) {
            try {
                ontology.addCoefficient(coefficient);
            }
            catch (AlreadyExistsException e) {
                log.warn(e.getMessage());
            }
        }
        ontology.setProcessGroups(RepoFactory.getGroupRepo().getProcessGroups());
        ontology.setCoefficientGroups(RepoFactory.getGroupRepo().getCoefficientGroups());
        ontology.setSourceRelations(RepoFactory.getRelationRepo().getSourceRelations());
        ontology.setCategoryTree(RepoFactory.getCategoryRepo().getCategoriesTree());
    }

    public void createMatrix() {
        /*dependencyMatrix = new CCSMatrix(processes.size(), processes.size());
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
        }*/
    }

    public void calculateCumulatedEcologicalFlows()
    {
        MatrixInverter inverter = dependencyMatrix.withInverter(LinearAlgebra.GAUSS_JORDAN);
        transitiveDependencyMatrix = inverter.inverse(LinearAlgebra.SPARSE_FACTORY);
        cumulativeEcologicalMatrix = transitiveDependencyMatrix.multiply(ecologicalMatrix);
    }

    protected void iterativeCalculationWithoutUncertainties()
    {
        HashSet<Process> processes = CarbonOntology.getInstance().getProcesses();
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
        ArrayList<Process> processes = new ArrayList<>(CarbonOntology.getInstance().getProcesses());

        dependencyMatrix = new CCSMatrix(processes.size(), processes.size());
        uncertaintyMatrix = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < processes.size(); i++) {
            for (DerivedRelation relation : processes.get(i).getDownstreamDerivedRelations()) {
                Process downStreamProcess = relation.getDestination();
                double value = dependencyMatrix.get(processes.indexOf(downStreamProcess), i);
                double uncertainty = uncertaintyMatrix.get(processes.indexOf(downStreamProcess), i);
                double coeffValue = relation.getCoeff().getValue().value * relation.getCoeff().getUnit().getConversionFactor();
                if (-1 == relation.getExponent()) {
                    coeffValue = 1 / coeffValue;
                }
                dependencyMatrix.set(
                    processes.indexOf(downStreamProcess),
                    i,
                    value+coeffValue
                );
                // @todo: check if the uncertainties should be added
                uncertaintyMatrix.set(
                    processes.indexOf(downStreamProcess),
                    i,
                    uncertainty+relation.getCoeff().getValue().uncertainty
                );
            }
        }
    }

    protected void createEcologicalMatrix() {
        ecologicalMatrix = new CCSMatrix(processes.size(), elementaryFlowTypes.size());
        ecologicalUncertaintyMatrix = new CCSMatrix(processes.size(), elementaryFlowTypes.size());

        for (int i = 0; i < processes.size(); i++) {
            HashMap<String, ElementaryFlow> flows = processes.get(i).getInputFlows();
            for (int j = 0; j < elementaryFlowTypes.size(); j++) {
                if (flows.containsKey(elementaryFlowTypes.get(j).getId())) {
                    ecologicalMatrix.set(i, j, flows.get(elementaryFlowTypes.get(j).getId()).getValue().value);
                    ecologicalUncertaintyMatrix.set(i, j, flows.get(elementaryFlowTypes.get(j).getId()).getValue().uncertainty);
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
        flowToImpactsMatrix = new CCSMatrix(impactTypes.size(), elementaryFlowTypes.size());
        flowToImpactsUncertaintyMatrix = new CCSMatrix(impactTypes.size(), elementaryFlowTypes.size());

        for (int i = 0; i < impactTypes.size(); i++) {
            HashMap<ElementaryFlowType, Value> components = impactTypes.get(i).getComponents();
            for (int j = 0; j < elementaryFlowTypes.size(); j++) {
                if (components.containsKey(elementaryFlowTypes.get(j))) {
                    flowToImpactsMatrix.set(i, j, components.get(elementaryFlowTypes.get(j)).value);
                    flowToImpactsUncertaintyMatrix.set(i, j, components.get(elementaryFlowTypes.get(j)).uncertainty);
                }
                else {
                    flowToImpactsMatrix.set(i, j, 0.0);
                    flowToImpactsUncertaintyMatrix.set(i, j, 0.0);
                }
            }
        }
    }

    protected void createCumulatedEcologicalFlows() throws AlreadyExistsException {
        for (int i = 0; i < processes.size(); i++) {
            for (int j = 0; j < elementaryFlowTypes.size(); j++) {
                double value = cumulativeEcologicalMatrix.get(i, j);
                if (value != 0.0) {
                    value *= processes.get(i).getUnit().getConversionFactor();
                    ElementaryFlow flow = new ElementaryFlow(
                            elementaryFlowTypes.get(j),
                            new Value(value, 0.0),
                            DataSource.CALCULATION);
                    processes.get(i).addCalculatedFlow(flow);
                }
            }
        }
    }

    protected void createImpacts() throws AlreadyExistsException {
        for (int i = 0; i < processes.size(); i++) {
            for (int j = 0; j < impactTypes.size(); j++) {
                double value = impactMatrix.get(i, j);
                if (value != 0.0) {
                    value *= processes.get(i).getUnit().getConversionFactor();
                    Impact impact = new Impact(
                            impactTypes.get(j),
                            new Value(value, 0.0));
                    processes.get(i).addImpact(impact);
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