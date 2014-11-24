package com.mycsense.carbondb.domain;

import com.mycsense.carbondb.AlreadyExistsException;
import com.mycsense.carbondb.NoElementFoundException;
import com.mycsense.carbondb.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class CarbonOntology {
    private static volatile CarbonOntology instance = null;

    protected HashMap<String, Group> processGroups;
    protected HashMap<String, Group> coefficientGroups;

    protected HashSet<Process> processes;
    protected HashSet<Coefficient> coefficients;

    protected HashMap<String, SourceRelation> sourceRelations;
    protected ArrayList<DerivedRelation> derivedRelations;

    protected Category elementaryFlowTypesTree;
    protected HashMap<String, ElementaryFlowType> elementaryFlowTypes;
    protected Category impactTypesTree;
    protected HashMap<String, ImpactType> impactTypes;

    protected HashMap<String, Reference> references;

    private CarbonOntology() {
        processGroups = new HashMap<>();
        coefficientGroups = new HashMap<>();
        processes = new HashSet<>();
        coefficients = new HashSet<>();
        references = new HashMap<>();
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

    public Category getElementaryFlowTypesTree() {
        return elementaryFlowTypesTree;
    }

    public void setElementaryFlowTypesTree(Category root) {
        elementaryFlowTypesTree = root;
        extractElementaryFlowTypesFromTree();
    }

    protected void extractElementaryFlowTypesFromTree() {
        if (null == elementaryFlowTypesTree) {
            throw new RuntimeException("The elementary flow types tree should be initialized"
                    + " before extracting the elementary flow types");
        }
        for (Object obj: elementaryFlowTypesTree.getChildren()) {
            elementaryFlowTypes = new HashMap<>();
            ElementaryFlowType type = (ElementaryFlowType) obj;
            elementaryFlowTypes.put(type.getId(), type);
        }
    }

    public HashMap<String, ElementaryFlowType> getElementaryFlowTypes() {
        return elementaryFlowTypes;
    }

    public ElementaryFlowType getElementaryFlowType(String id) {
        return elementaryFlowTypes.get(id);
    }

    public Category getImpactTypesTree() {
        return impactTypesTree;
    }

    public void setImpactTypesTree(Category root) {
        impactTypesTree = root;
        extractImpactTypesFromTree();
    }

    protected void extractImpactTypesFromTree() {
        if (null == impactTypesTree) {
            throw new RuntimeException("The impact types tree should be initialized"
                    + " before extracting the impact types");
        }
        for (Object obj: impactTypesTree.getChildren()) {
            impactTypes = new HashMap<>();
            ImpactType type = (ImpactType) obj;
            impactTypes.put(type.getId(), type);
        }
    }

    public HashMap<String, ImpactType> getImpactTypes() {
        return impactTypes;
    }

    public ImpactType getImpactType(String id) {
        return impactTypes.get(id);
    }

    public HashMap<String, Reference> getReferences() {
        return references;
    }

    public void setReferences(HashMap<String, Reference> references) {
        this.references = references;
    }

    public Reference getReference(String id) throws NotFoundException {
        if (!references.containsKey(id)) {
            throw new NotFoundException("The reference with the id " + id + " was not found");
        }
        return references.get(id);
    }

    public HashMap<String, Group> getProcessGroups() {
        return processGroups;
    }

    public void setProcessGroups(HashMap<String, Group> processGroups) {
        this.processGroups = processGroups;
    }

    public Group getProcessGroup(String id) {
        return processGroups.get(id);
    }

    public HashMap<String, Group> getCoefficientGroups() {
        return coefficientGroups;
    }

    public void setCoefficientGroups(HashMap<String, Group> coefficientGroups) {
        this.coefficientGroups = coefficientGroups;
    }

    public Group getCoefficientGroup(String id) {
        return coefficientGroups.get(id);
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
