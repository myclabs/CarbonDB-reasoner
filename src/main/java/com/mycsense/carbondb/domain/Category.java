package com.mycsense.carbondb.domain;

import java.util.ArrayList;

public class Category
{
    protected String uri;
    protected String label;
    protected ArrayList<Object> children = new ArrayList<>();
    protected Category parent;

    public Category() {
        uri = "";
    }

    public Category(String uri) {
        this.uri = uri;
    }

    public Category(String uri, String label) {
        this.uri = uri;
        this.label = label;
    }

    public Category(String uri, String label, Category parent) {
        this.uri = uri;
        this.label = label;
        this.parent = parent;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public String toString() {
        return uri;
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