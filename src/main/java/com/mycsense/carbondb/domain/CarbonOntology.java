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
import com.mycsense.carbondb.NoElementFoundException;
import com.mycsense.carbondb.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class CarbonOntology {
    private static volatile CarbonOntology instance = null;

    protected Category categoryTree;

    protected HashMap<String, Group> processGroups;
    protected HashMap<String, Group> coefficientGroups;

    protected HashSet<Process> processes;
    protected HashSet<Coefficient> coefficients;

    protected HashMap<String, SourceRelation> sourceRelations;
    protected ArrayList<DerivedRelation> derivedRelations;

    protected TypeCategory elementaryFlowTypesTree;
    protected HashMap<String, ElementaryFlowType> elementaryFlowTypes;
    protected TypeCategory impactTypesTree;
    protected HashMap<String, ImpactType> impactTypes;
    protected HashSet<RelationType> relationTypes;

    protected HashSet<Reference> references;

    private CarbonOntology() {
        processGroups = new HashMap<>();
        coefficientGroups = new HashMap<>();
        processes = new HashSet<>();
        coefficients = new HashSet<>();
        references = new HashSet<>();
        sourceRelations = new HashMap<>();
        derivedRelations = new ArrayList<>();
        elementaryFlowTypes = new HashMap<>();
        impactTypes = new HashMap<>();
    }

    public static CarbonOntology getInstance() {
        if (null == CarbonOntology.instance) {
            synchronized (CarbonOntology.class) {
                if (null == CarbonOntology.instance) {
                    CarbonOntology.instance = new CarbonOntology();
                }
            }
        }
        return CarbonOntology.instance;
    }

    public void clear() {
        processGroups = new HashMap<>();
        coefficientGroups = new HashMap<>();
        processes = new HashSet<>();
        coefficients = new HashSet<>();
        references = new HashSet<>();
        sourceRelations = new HashMap<>();
        derivedRelations = new ArrayList<>();
        elementaryFlowTypes = new HashMap<>();
        impactTypes = new HashMap<>();
    }

    public Coefficient findCoefficient(Dimension keywords, Unit unit) throws NoElementFoundException {
        Coefficient temporaryCoefficient = new Coefficient(keywords, unit, new Value(0.0,0.0));
        if (coefficients.contains(temporaryCoefficient)) {
            for (Coefficient coefficient: coefficients) {
                if (coefficient.equals(temporaryCoefficient)) {
                    return coefficient;
                }
            }
        }
        throw new NoElementFoundException("No coefficient found with keywords " + keywords + " and unit " + unit);
    }

    public Process findProcess(Dimension keywords, Unit unit) throws NoElementFoundException {
        Process temporaryProcess = new Process(keywords, unit);
        if (processes.contains(temporaryProcess)) {
            for (Process process: processes) {
                if (process.equals(temporaryProcess)) {
                    return process;
                }
            }
        }
        throw new NoElementFoundException("No process found with keywords " + keywords + " and unit " + unit);
    }

    public Category getCategoryTree() {
        return categoryTree;
    }

    public void setCategoryTree(Category categoryTree) {
        this.categoryTree = categoryTree;
    }

    public TypeCategory getElementaryFlowTypesTree() {
        return elementaryFlowTypesTree;
    }

    public void setElementaryFlowTypesTree(TypeCategory root) {
        elementaryFlowTypesTree = root;
        extractElementaryFlowTypesFromTree();
    }

    protected void extractElementaryFlowTypesFromTree() {
        if (null == elementaryFlowTypesTree) {
            throw new RuntimeException("The elementary flow types tree should be initialized"
                    + " before extracting the elementary flow types");
        }
        elementaryFlowTypes = new HashMap<>();
        for (Object categoryObj : elementaryFlowTypesTree.getChildren()) {
            Category category = (Category) categoryObj;
            for (Object obj : category.getChildren()) {
                ElementaryFlowType type = (ElementaryFlowType) obj;
                elementaryFlowTypes.put(type.getId(), type);
            }
        }
    }

    public HashMap<String, ElementaryFlowType> getElementaryFlowTypes() {
        return elementaryFlowTypes;
    }

    public ElementaryFlowType getElementaryFlowType(String id) throws NotFoundException {
        if (!elementaryFlowTypes.containsKey(id)) {
            throw new NotFoundException("The elementary flow type " + id + " could not be found");
        }
        return elementaryFlowTypes.get(id);
    }

    public TypeCategory getImpactTypesTree() {
        return impactTypesTree;
    }

    public void setImpactTypesTree(TypeCategory root) {
        impactTypesTree = root;
        extractImpactTypesFromTree();
    }

    protected void extractImpactTypesFromTree() {
        if (null == impactTypesTree) {
            throw new RuntimeException("The impact types tree should be initialized"
                    + " before extracting the impact types");
        }
        impactTypes = new HashMap<>();
        for (Object categoryObj : impactTypesTree.getChildren()) {
            Category category = (Category) categoryObj;
            for (Object obj : category.getChildren()) {
                ImpactType type = (ImpactType) obj;
                impactTypes.put(type.getId(), type);
            }
        }
    }

    public HashMap<String, ImpactType> getImpactTypes() {
        return impactTypes;
    }

    public ImpactType getImpactType(String id) throws NotFoundException {
        if (!impactTypes.containsKey(id)) {
            throw new NotFoundException("The impact type " + id + " could not be found");
        }
        return impactTypes.get(id);
    }

    public HashSet<RelationType> getRelationTypes() {
        return relationTypes;
    }

    public void setRelationTypes(HashSet<RelationType> relationTypes) {
        this.relationTypes = relationTypes;
    }

    public HashSet<Reference> getReferences() {
        return references;
    }

    public void setReferences(HashSet<Reference> references) {
        this.references = references;
    }

    public HashMap<String, Group> getProcessGroups() {
        return processGroups;
    }

    public void setProcessGroups(HashMap<String, Group> processGroups) {
        this.processGroups = processGroups;
    }

    public Group getProcessGroup(String id) throws NotFoundException {
        if (!processGroups.containsKey(id)) {
            throw new NotFoundException("The process group " + id + " could not be found");
        }
        return processGroups.get(id);
    }

    public HashMap<String, Group> getCoefficientGroups() {
        return coefficientGroups;
    }

    public void setCoefficientGroups(HashMap<String, Group> coefficientGroups) {
        this.coefficientGroups = coefficientGroups;
    }

    public Group getCoefficientGroup(String id) {
        if (!coefficientGroups.containsKey(id)) {

        }
        return coefficientGroups.get(id);
    }

    public HashMap<String, Group> getGroups() {
        HashMap<String, Group> groups = new HashMap<>();
        groups.putAll(processGroups);
        groups.putAll(coefficientGroups);
        return groups;
    }

    public HashSet<Process> getProcesses() {
        return processes;
    }

    public void addProcess(Process process) throws AlreadyExistsException {
        if (processes.contains(process)) {
            throw new AlreadyExistsException("The process with keywords " + process
                    + " whith id: " + process.getId() + " already exists");
        }
        processes.add(process);
    }

    public HashSet<Coefficient> getCoefficients() {
        return coefficients;
    }

    public void addCoefficient(Coefficient coefficient) throws AlreadyExistsException {
        if (coefficients.contains(coefficient)) {
            throw new AlreadyExistsException("The coefficient with keywords " + coefficient
                    + " whith id: " + coefficient.getId() + " already exists");
        }
        coefficients.add(coefficient);
    }

    public HashMap<String, SourceRelation> getSourceRelations() {
        return sourceRelations;
    }

    public void setSourceRelations(HashMap<String, SourceRelation> sourceRelations) {
        this.sourceRelations = sourceRelations;
    }

    public ArrayList<DerivedRelation> getDerivedRelations() {
        return derivedRelations;
    }

    public void addDerivedRelation(DerivedRelation derivedRelation) {
        derivedRelations.add(derivedRelation);
    }
}
