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

/**
 * This singleton holds the carbon ontology elements
 * It is somehow similar to an entity manager in an ORM
 */
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

    protected HashMap<String, Dimension> dimensions;

    protected HashMap<String, Reference> references;

    private CarbonOntology() {
        clear();
    }

    /**
     * Returns the singleton instance
     * @return CarbonOntology
     */
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

    /**
     * Clears the stored ontology
     */
    public void clear() {
        processGroups = new HashMap<>();
        coefficientGroups = new HashMap<>();
        processes = new HashSet<>();
        coefficients = new HashSet<>();
        references = new HashMap<>();
        sourceRelations = new HashMap<>();
        derivedRelations = new ArrayList<>();
        elementaryFlowTypes = new HashMap<>();
        impactTypes = new HashMap<>();
        dimensions = new HashMap<>();
    }

    /**
     * Returns a coefficient that matches a set of keywords and an unit
     * @param keywords A set of keywords
     * @param unit An unit
     * @return The matching coefficient
     * @throws NoElementFoundException
     */
    public Coefficient findCoefficient(Dimension keywords, Unit unit) throws NoElementFoundException {
        Coefficient temporaryCoefficient = new Coefficient(keywords.keywords, unit, new Value(0.0,0.0));
        if (coefficients.contains(temporaryCoefficient)) {
            for (Coefficient coefficient: coefficients) {
                if (coefficient.equals(temporaryCoefficient)) {
                    return coefficient;
                }
            }
        }
        throw new NoElementFoundException("No coefficient found with keywords " + keywords + " and unit " + unit);
    }

    /**
     * Returns a process that matches a set of keywords and an unit
     * @param keywords A set of keywords
     * @param unit An unit
     * @return The matching process
     * @throws NoElementFoundException
     */
    public Process findProcess(Dimension keywords, Unit unit) throws NoElementFoundException {
        Process temporaryProcess = new Process(keywords.keywords, unit);
        if (processes.contains(temporaryProcess)) {
            for (Process process: processes) {
                if (process.equals(temporaryProcess)) {
                    return process;
                }
            }
        }
        throw new NoElementFoundException("No process found with keywords " + keywords + " and unit " + unit);
    }

    /**
     * Returns the group category tree, this tree can have an arbitrary number of level
     * @return the group category tree
     */
    public Category getCategoryTree() {
        return categoryTree;
    }

    /**
     * Sets the group category tree
     * @param categoryTree the first node of the category tree
     */
    public void setCategoryTree(Category categoryTree) {
        this.categoryTree = categoryTree;
    }

    /**
     * The elementary flow types tree categorized the flow types
     * in a one level hierarchy
     *
     * @return the root of the tree whose children
     *             are only elementary flow types
     */
    public TypeCategory getElementaryFlowTypesTree() {
        return elementaryFlowTypesTree;
    }

    /**
     * The elementary flow types tree categorized the flow types
     * in a one level hierarchy
     *
     * @param root the root of the tree whose children
     *             are only elementary flow types
     */
    public void setElementaryFlowTypesTree(TypeCategory root) {
        elementaryFlowTypesTree = root;
        extractElementaryFlowTypesFromTree();
    }

    /**
     * Extract the elementary flow types from the elementary flow type tree
     * and add them to a map where the key is the type id
     * @throw RuntimeException if the tree is not initialized
     */
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

    /**
     * Returns the elementary flow types in a map where the key is the type id,
     * the elementary flow types tree should be set first (the types
     * are extracted from the tree)
     *
     * @return a map containing the elementary flow types indexed with their id
     */
    public HashMap<String, ElementaryFlowType> getElementaryFlowTypes() {
        return elementaryFlowTypes;
    }

    /**
     * Return an elementary flow type from it's id
     *
     * @param id the id of the elementary flow type
     * @return an elementary flow type
     * @throws NotFoundException
     */
    public ElementaryFlowType getElementaryFlowType(String id) throws NotFoundException {
        if (!elementaryFlowTypes.containsKey(id)) {
            throw new NotFoundException("The elementary flow type " + id + " could not be found");
        }
        return elementaryFlowTypes.get(id);
    }

    /**
     * The impact types tree categorized the impact types
     * in a one level hierarchy
     *
     * @return the root of the tree whose children
     *             are only elementary flow types
     */
    public TypeCategory getImpactTypesTree() {
        return impactTypesTree;
    }

    /**
     * The impact types tree categorized the impact types
     * in a one level hierarchy
     *
     * @param root the root of the tree whose children
     *             are only impact types
     */
    public void setImpactTypesTree(TypeCategory root) {
        impactTypesTree = root;
        extractImpactTypesFromTree();
    }

    /**
     * Extract the impact types from the impact type tree
     * and add them to a map where the key is the type id
     *
     * @throw RuntimeException if the tree is not initialized
     */
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

    /**
     * @return the impact types in a map where the key is the type id,
     *         the impact types tree should be set first (the types
     *         are extracted from the tree)
     */
    public HashMap<String, ImpactType> getImpactTypes() {
        return impactTypes;
    }

    /**
     * Return an impact type from it's id
     *
     * @param id the id of the impact type
     * @return an impact type
     * @throws NotFoundException
     */
    public ImpactType getImpactType(String id) throws NotFoundException {
        if (!impactTypes.containsKey(id)) {
            throw new NotFoundException("The impact type " + id + " could not be found");
        }
        return impactTypes.get(id);
    }

    /**
     * @return a set containing the relation types
     */
    public HashSet<RelationType> getRelationTypes() {
        return relationTypes;
    }

    /**
     * @param relationTypes a set containing the relation types
     */
    public void setRelationTypes(HashSet<RelationType> relationTypes) {
        this.relationTypes = relationTypes;
    }

    /**
     * @return a map containing the references indexed by their ids
     */
    public HashMap<String, Reference> getReferences() {
        return references;
    }

    /**
     * Finds and returns a reference from it's id
     *
     * @param id the id of the reference
     * @return a reference
     * @throws NotFoundException
     */
    public Reference getReference(String id) throws NotFoundException {
        if (!references.containsKey(id)) {
            throw new NotFoundException("The reference " + id + " could not be found");
        }
        return references.get(id);
    }

    /**
     * @param references a map containing the references indexed by their ids
     */
    public void setReferences(HashMap<String, Reference> references) {
        this.references = references;
    }

    /**
     * @return a map containing the process groups indexed by their ids
     */
    public HashMap<String, Group> getProcessGroups() {
        return processGroups;
    }

    /**
     * @param processGroups a map containing the process groups indexed by their ids
     */
    public void setProcessGroups(HashMap<String, Group> processGroups) {
        this.processGroups = processGroups;
    }

    /**
     * Finds and returns a process group from it's id
     *
     * @param id the id of the process group
     * @return a process group
     * @throws NotFoundException
     */
    public Group getProcessGroup(String id) throws NotFoundException {
        if (!processGroups.containsKey(id)) {
            throw new NotFoundException("The process group " + id + " could not be found");
        }
        return processGroups.get(id);
    }

    /**
     * @return a map containing the coefficient groups indexed by their ids
     */
    public HashMap<String, Group> getCoefficientGroups() {
        return coefficientGroups;
    }

    /**
     * @param coefficientGroups a map containing the coefficient groups indexed by their ids
     */
    public void setCoefficientGroups(HashMap<String, Group> coefficientGroups) {
        this.coefficientGroups = coefficientGroups;
    }

    /**
     * Finds and returns a coefficient group from it's id
     *
     * @param id the id of the coefficient group
     * @return a coefficient group
     * @throws NotFoundException
     */
    public Group getCoefficientGroup(String id) throws NotFoundException {
        if (!coefficientGroups.containsKey(id)) {
            throw new NotFoundException("The coefficient group " + id + " could not be found");
        }
        return coefficientGroups.get(id);
    }

    /**
     * Returns all the process and coefficient groups in one map
     *
     * @return a map containing the process and the coefficient groups indexed by their ids
     */
    public HashMap<String, Group> getGroups() {
        HashMap<String, Group> groups = new HashMap<>();
        groups.putAll(processGroups);
        groups.putAll(coefficientGroups);
        return groups;
    }

    /**
     * @return a set containing all the single processes
     */
    public HashSet<Process> getProcesses() {
        return processes;
    }

    /**
     * Add a process only if no process already exists
     * with the same set of keywords set and the same unit
     *
     * @param process the new process
     * @throws AlreadyExistsException if there already is a process with
     *                                the same set of keywords and the same unit
     */
    public void addProcess(Process process) throws AlreadyExistsException {
        if (processes.contains(process)) {
            throw new AlreadyExistsException("The process with keywords " + process
                    + " whith id: " + process.getId() + " already exists");
        }
        processes.add(process);
    }

    /**
     * @return a set containing all the single coefficients
     */
    public HashSet<Coefficient> getCoefficients() {
        return coefficients;
    }

    /**
     * Add a coefficient only if no coefficient already exists
     * with the same set of keywords set and the same unit
     *
     * @param coefficient the new coefficient
     * @throws AlreadyExistsException if there already is a coefficient with
     *                                the same set of keywords and the same unit
     */
    public void addCoefficient(Coefficient coefficient) throws AlreadyExistsException {
        if (coefficients.contains(coefficient)) {
            throw new AlreadyExistsException("The coefficient with keywords " + coefficient
                    + " whith id: " + coefficient.getId() + " already exists");
        }
        coefficients.add(coefficient);
    }

    /**
     * @return a map containing the source relations indexed by their ids
     */
    public HashMap<String, SourceRelation> getSourceRelations() {
        return sourceRelations;
    }

    /**
     * @param sourceRelations a map containing the source relations indexed by their ids
     */
    public void setSourceRelations(HashMap<String, SourceRelation> sourceRelations) {
        this.sourceRelations = sourceRelations;
    }

    /**
     * Returns the derived relations. Those relations are only available after the reasoner was launched.
     *
     * @return a list of the derived relations
     */
    public ArrayList<DerivedRelation> getDerivedRelations() {
        return derivedRelations;
    }

    /**
     * @param derivedRelation a derived relation
     */
    public void addDerivedRelation(DerivedRelation derivedRelation) {
        derivedRelations.add(derivedRelation);
    }

    /**
     * @param dimensions a map containing the dimensions indexed by their ids
     */
    public void setDimensions(HashMap<String, Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * @return a map containing the dimensions indexed by their ids
     */
    public HashMap<String, Dimension> getDimensions() {
        return dimensions;
    }

    /**
     * Finds and returns a dimension form it's id
     *
     * @param id the id of the dimension
     * @return a dimension
     * @throws NotFoundException
     */
    public Dimension getDimension(String id) throws NotFoundException {
        if (!dimensions.containsKey(id)) {
            throw new NotFoundException("The dimension " + id + " could not be found");
        }
        return dimensions.get(id);
    }
}
