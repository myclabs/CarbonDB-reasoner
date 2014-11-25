package com.mycsense.carbondb.domain;

public class TypeCategory extends Category {
    protected Unit unit;

    public TypeCategory(Unit unit) {
        this.unit = unit;
    }

    public TypeCategory(String id, Unit unit) {
        super(id);
        this.unit = unit;
    }

    public TypeCategory(String id, String label, Unit unit) {
        super(id, label);
        this.unit = unit;
    }

    public TypeCategory(String id, String label, Category parent, Unit unit) {
        super(id, label, parent);
        this.unit = unit;
    }
}
