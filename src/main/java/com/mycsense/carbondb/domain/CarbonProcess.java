package com.mycsense.carbondb.domain;

import com.hp.hpl.jena.rdf.model.Resource;

import java.util.HashMap;

public class CarbonProcess extends SingleElement {
    HashMap<Resource, Value> emissions;
    HashMap<String, Value> impacts;

    public CarbonProcess(Dimension keywords) {
        super(keywords);
        emissions = new HashMap<>();
        impacts = new HashMap<>();
    }

    public HashMap<Resource, Value> getEmissions() {
        return emissions;
    }

    public void setEmissions(HashMap<Resource, Value> emissions) {
        this.emissions = emissions;
    }

    public HashMap<String, Value> getImpacts() {
        return impacts;
    }

    public void setImpacts(HashMap<String, Value> impacts) {
        this.impacts = impacts;
    }
}