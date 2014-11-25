package com.mycsense.carbondb.domain;

import java.util.HashSet;

public class Coefficient extends SingleElement {
    protected Value value;
    protected HashSet<DerivedRelation> derivedRelations;

    public Coefficient(Dimension keywords, Unit unit, Value value) {
        super(keywords, unit);
        this.value = value;
        derivedRelations = new HashSet<>();
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public void addDerivedRelation(DerivedRelation derivedRelation) {
        derivedRelations.add(derivedRelation);
    }

    public HashSet<DerivedRelation> getDerivedRelations() {
        return derivedRelations;
    }
}
