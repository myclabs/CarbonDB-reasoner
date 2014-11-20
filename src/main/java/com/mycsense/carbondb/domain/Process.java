package com.mycsense.carbondb.domain;

import com.mycsense.carbondb.AlreadyExistsException;

import java.util.HashMap;
import java.util.HashSet;

public class Process extends SingleElement {
    protected HashMap<String, Impact> impacts;
    protected HashMap<String, ElementaryFlow> flows;
    protected HashSet<DerivedRelation> downstreamDerivedRelations;
    protected HashSet<DerivedRelation> upstreamDerivedRelations;

    public Process(Dimension keywords, Unit unit) {
        super(keywords, unit);
        impacts = new HashMap<>();
        flows = new HashMap<>();
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

    public HashMap<String, ElementaryFlow> getFlows() {
        return flows;
    }

    public void addFlow(ElementaryFlow flow) throws AlreadyExistsException {
        if (flows.containsKey(flow.getType().getId())) {
            throw new AlreadyExistsException("The process " + id + " already has an elementary"
                    + " flow for the elementary flow type " + flow.getType().getId());
        }
        flows.put(flow.getType().getId(), flow);
    }

    public void setFlows(HashMap<String, ElementaryFlow> flows) {
        this.flows = flows;
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