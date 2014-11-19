package com.mycsense.carbondb.domain;

public class ElementaryFlow {
    protected ElementaryFlowType type;
    protected Value value;

    public ElementaryFlow(ElementaryFlowType type, Value value) {
        this.type = type;
        this.value = value;
    }

    public ElementaryFlowType getType() {
        return type;
    }

    public void setType(ElementaryFlowType type) {
        this.type = type;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
