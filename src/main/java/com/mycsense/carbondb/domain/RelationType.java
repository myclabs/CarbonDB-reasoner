package com.mycsense.carbondb.domain;

import com.mycsense.carbondb.domain.relation.Type;

public class RelationType {
    protected String id;
    protected String label;
    protected String comment;
    protected Type type;

    public RelationType(String id, String label, Type type) {
        this.id = id;
        this.label = label;
        this.comment = "";
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
