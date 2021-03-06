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

package com.mycsense.carbondb.domain;

import com.mycsense.carbondb.AlreadyExistsException;
import com.mycsense.carbondb.domain.elementaryFlow.DataSource;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * <p>
 * This class calculates the cumulative elementary flows and the impacts for all the processes in the ontology.
 * The calculation is done using matrices products until the results is below a threshold.
 * </p>
 * <p>
 *     The workflow is as follows:
 *     <ul>
 *          <li>
 *              create the dependency matrix between the processes: <code>R</code>,
 *              also called the technology matrix
 *          </li>
 *          <li>
 *              create a matrix containing all the elementary flows: <code>C</code>,
 *              also called the ecology matrix
 *          </li>
 *          <li>
 *              create a matrix containing the binding between an elementary flow and an impact
 *          </li>
 *          <li>
 *              calculate the cumulative technology matrix
 *              <code></>R<sup>0</sup> + R<sup>1</sup> + ... + R<sup>n</sup></code>
 *              where n is large enough that <code>R<sup>n</sup> - R<sup>n-1</sup></code>
 *              is lower than a the threshold
 *          </li>
 *          <li>
 *              multiply the cumulative technology matrix with the ecology matrix,
 *              thus obtaining a matrix that contains the cumulative elementary flows
 *          </li>
 *          <li>
 *              multiply the cumulative elementary flow matrix with the flow to impact matrix
 *          </li>
 *     </ul>
 * </p>
 */
public class Calculation {
    protected Matrix dependencyMatrix, transitiveDependencyMatrix;
    protected Matrix uncertaintyMatrix, transitiveUncertaintyMatrix;
    protected Matrix ecologicalMatrix, cumulativeEcologicalMatrix;
    protected Matrix flowToImpactsMatrix, flowToImpactsUncertaintyMatrix;
    protected Matrix impactMatrix;
    protected Matrix ecologicalUncertaintyMatrix, cumulativeEcologicalUncertaintyMatrix;
    protected Double threshold = 0.1;

    protected CarbonOntology ontology;

    protected ArrayList<ImpactType> impactTypes;
    protected ArrayList<ElementaryFlowType> elementaryFlowTypes;

    protected ArrayList<Process> processes;

    private final Logger log = LoggerFactory.getLogger(Calculation.class);

    public Calculation() {
        ontology = CarbonOntology.getInstance();
    }

    /**
     * Launch the calculation, see the workflow describe above.
     */
    public void run() {
        elementaryFlowTypes = new ArrayList<>(ontology.getElementaryFlowTypes().values());
        processes = new ArrayList<>(ontology.getProcesses());
        impactTypes = new ArrayList<>(ontology.getImpactTypes().values());

        log.info("Creating ecological matrices");
        createEcologicalMatrices();
        createFlowToImpactsMatrices();

        log.info("Creating matrix");
        createProcessMatrices();

        log.info("Calculating cumulative flows");
        iterativeCalculationWithoutUncertainties();
        cumulativeEcologicalMatrix = transitiveDependencyMatrix.multiply(ecologicalMatrix);

        log.info("Calculating impacts");
        impactMatrix = cumulativeEcologicalMatrix.multiply(flowToImpactsMatrix.transpose());

        // version with uncertainty calculation
        //createProcessMatrices();
        //iterativeCalculation();
        //ecologicalCumulatedFlowCalculation();

        // matrix inversion method
        //createMatrix();
        //calculateCumulatedEcologicalFlows();
    }

    /**
     * Creates the processes matrices (i.e.: the etchnology matrix) from the derived relations:
     * one with the coefficients values and one with the coefficients uncertainties.
     * If the relation has an negative exponent, the coefficient gets inverted.
     */
    protected void createProcessMatrices() {
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

    /**
     * Creates the ecology values and uncertainties matrices from the processes elementary flows.
     */
    protected void createEcologicalMatrices() {
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

    /**
     * Creates the values and uncertainties matrices containing the mapping from elementary flows to impacts.
     */
    protected void createFlowToImpactsMatrices() {
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

    /**
     * Calculates iteratively the cumulative technology matrix without calculating the uncertainties.
     */
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

    /**
     * Returns a boolean indicating if the difference between the two given matrices
     * is greater (true) or lower (false) than the threshold.
     *
     * @param a matrix a
     * @param b matrix b
     * @return true if the a - b is lower than the threshold, false otherwise
     */
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

    /**
     * Creates the cumulative elementary flows for every process.
     *
     * @throws AlreadyExistsException
     */
    public void createCalculatedElementaryFlows() throws AlreadyExistsException {
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

    /**
     * Creates the (calculated) impacts for every process.
     *
     * @throws AlreadyExistsException
     */
    public void createImpacts() throws AlreadyExistsException {
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

    // Calculation with uncertainties

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
}
