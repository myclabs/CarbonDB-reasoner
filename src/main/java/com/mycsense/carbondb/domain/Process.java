package com.mycsense.carbondb.domain;

import java.util.ArrayList;
import java.util.HashMap;

public class Process extends SingleElement {
    protected HashMap<String, Impact> impacts;
    protected ArrayList<ElementaryFlow> flows;

    public Process(Dimension keywords) {
        super(keywords);
        impacts = new HashMap<>();
        flows = new ArrayList<>();
    }

    public HashMap<String, Impact> getImpacts() {
        return impacts;
    }

    public void setImpacts(HashMap<String, Impact> impacts) {
        this.impacts = impacts;
    }

    public void addImpact(Impact impact) {
        if (impacts.containsKey(impact.getType().getId())) {
            // the new impact type is already filled for this process
            // shall we throw an exception or add a warning to the logger?
        }
        impacts.put(impact.getType().getId(), impact);
    }

    public ArrayList<ElementaryFlow> getFlows() {
        return flows;
    }

    public void addFlow(ElementaryFlow flow) {
        flows.add(flow);
    }

    public void setFlows(ArrayList<ElementaryFlow> flows) {
        this.flows = flows;
    }
}