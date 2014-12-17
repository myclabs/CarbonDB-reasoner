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

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class Process extends SingleElement {
    protected HashMap<String, Impact> impacts;
    protected HashMap<String, ElementaryFlow> inputFlows;
    protected HashMap<String, ElementaryFlow> calculatedFlows;
    protected HashSet<DerivedRelation> downstreamDerivedRelations;
    protected HashSet<DerivedRelation> upstreamDerivedRelations;

    public Process(TreeSet<Keyword> keywords, Unit unit) {
        super(keywords, unit);
        impacts = new HashMap<>();
        inputFlows = new HashMap<>();
        calculatedFlows = new HashMap<>();
        downstreamDerivedRelations = new HashSet<>();
        upstreamDerivedRelations = new HashSet<>();
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