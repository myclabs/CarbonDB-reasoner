package com.mycsense.carbondb.domain;

public class Impact {
    protected ImpactType type;
    protected Value value;

    public Impact(ImpactType type, Value value) {
        this.type = type;
        this.value = value;
    }

    public ImpactType getType() {
        return type;
    }

    public void setType(ImpactType type) {
        this.type = type;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
