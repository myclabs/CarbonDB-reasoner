package com.mycsense.carbondb;

import java.util.ArrayList;

public class Category
{
    protected String uri;
    protected String label;
    protected ArrayList<Object> children = new ArrayList<Object>();

    public Category() {
        uri = new String();
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
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String name) {
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