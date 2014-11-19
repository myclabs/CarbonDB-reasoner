package com.mycsense.carbondb.domain;

public class ElementaryFlowType {
    protected String id;
    protected String label;
    protected Unit unit;

    public ElementaryFlowType(String id, String label, Unit unit) {
        this.id = id;
        this.label = label;
        this.unit = unit;
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
}
