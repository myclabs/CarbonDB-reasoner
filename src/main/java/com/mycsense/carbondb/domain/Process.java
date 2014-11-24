package com.mycsense.carbondb.domain;

import com.mycsense.carbondb.AlreadyExistsException;

import java.util.HashMap;
import java.util.HashSet;

public class Process extends SingleElement {
    protected HashMap<String, Impact> impacts;
    protected HashMap<String, ElementaryFlow> inputFlows;
    protected HashMap<String, ElementaryFlow> calculatedFlows;
    protected HashSet<DerivedRelation> downstreamDerivedRelations;
    protected HashSet<DerivedRelation> upstreamDerivedRelations;

    public Process(Dimension keywords, Unit unit) {
        super(keywords, unit);
        impacts = new HashMap<>();
        inputFlows = new HashMap<>();
    }

    public HashMap<String, Impact> getImpacts() {
        return impacts;
    }

    public void setImpacts(HashMap<String, Impact> impacts) {
        this.impacts = impacts;
    }

    public void addImpact(Impact impact) throws AlreadyExistsException {
        if (impacts.containsKey(impact.getType().getId())) {
            throw new AlreadyExistsException("The process " + id + " already has an impact for the impact type " + impact.getType().getId());
        }
        impacts.put(impact.getType().getId(), impact);
    }

    public HashMap<String, ElementaryFlow> getInputFlows() {
        return inputFlows;
    }

    public void addInputFlow(ElementaryFlow flow) throws AlreadyExistsException {
        if (inputFlows.containsKey(flow.getType().getId())) {
            throw new AlreadyExistsException("The process " + id + " already has an elementary"
                    + " flow for the elementary flow type " + flow.getType().getId());
        }
        inputFlows.put(flow.getType().getId(), flow);
    }

    public HashMap<String, ElementaryFlow> getCalculatedFlows() {
        return calculatedFlows;
    }

    public void addCalculatedFlow(ElementaryFlow flow) throws AlreadyExistsException {
        if (calculatedFlows.containsKey(flow.getType().getId())) {
            throw new AlreadyExistsException("The process " + id + " already has an elementary"
                    + " flow for the elementary flow type " + flow.getType().getId());
        }
        calculatedFlows.put(flow.getType().getId(), flow);
    }

    public boolean hasCalculatedElementaryFlow(ElementaryFlowType type) {
        return calculatedFlows.containsKey(type.getId());
    }

    public ElementaryFlow getElementaryFlow(ElementaryFlowType type) {
        return calculatedFlows.get(type.getId());
    }

    public void addDownstreamDerivedRelation(DerivedRelation derivedRelation) {
        downstreamDerivedRelations.add(derivedRelation);
    }

    public HashSet<DerivedRelation> getDownstreamDerivedRelations() {
        return downstreamDerivedRelations;
    }

    public void addUpstreamDerivedRelation(DerivedRelation derivedRelation) {
        upstreamDerivedRelations.add(derivedRelation);
    }

    public HashSet<DerivedRelation> getUpstreamDerivedRelations() {
        return upstreamDerivedRelations;
    }
}