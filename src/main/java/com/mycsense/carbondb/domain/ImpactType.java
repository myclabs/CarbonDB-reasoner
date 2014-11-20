package com.mycsense.carbondb.domain;

import java.util.HashMap;

public class ImpactType {
    protected String id;
    protected String label;
    protected Unit unit;
    protected HashMap<ElementaryFlowType, Value> components;

    public ImpactType(String id, String label, Unit unit) {
        this.id = id;
        this.label = label;
        this.unit = unit;
        components = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public HashMap<ElementaryFlowType, Value> getComponents() {
        return components;
    }

    public void setComponents(HashMap<ElementaryFlowType, Value> components) {
        this.components = components;
    }

    public void addComponent(ElementaryFlowType flowType, Value value) {
        components.put(flowType, value);
    }
}
