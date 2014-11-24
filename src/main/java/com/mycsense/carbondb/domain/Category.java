package com.mycsense.carbondb.domain;

import java.util.ArrayList;

public class Category
{
    protected String id;
    protected String label;
    protected ArrayList<Object> children = new ArrayList<>();
    protected Category parent;

    public Category() {
        id = "";
    }

    public Category(String id) {
        this.id = id;
    }

    public Category(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public Category(String id, String label, Category parent) {
        this.id = id;
        this.label = label;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public void setURI(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public ArrayList<Object> getChildren() {
        return children;
    }

    public void addChild(Object child) {
        children.add(child);
    }
}