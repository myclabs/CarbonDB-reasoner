package com.mycsense.carbondb.domain;

public class Coefficient extends SingleElement {
    protected Value value;

    public Coefficient(Dimension keywords, Value value) {
        super(keywords);
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
