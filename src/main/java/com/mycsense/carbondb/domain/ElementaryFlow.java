package com.mycsense.carbondb.domain;

import com.mycsense.carbondb.domain.elementaryFlow.DataSource;

public class ElementaryFlow {
    protected ElementaryFlowType type;
    protected Value value;
    protected DataSource dataSource;

    public ElementaryFlow(ElementaryFlowType type, Value value) {
        this(type, value, DataSource.INPUT);
    }

    public ElementaryFlow(ElementaryFlowType type, Value value, DataSource dataSource) {
        this.type = type;
        this.value = value;
        this.dataSource = dataSource;
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

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
