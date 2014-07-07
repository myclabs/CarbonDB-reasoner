package com.mycsense.carbondb; 

public class Group
{
    public DimensionSet dimSet;
    public DimensionSet elements;

    protected String label;
    protected String uri;
    protected String id;

    public Group() {
        dimSet = new DimensionSet();
        elements = new DimensionSet();
    }

    public Group(Dimension... dimensions) {
        dimSet = new DimensionSet();
        for (Dimension dimension: dimensions) {
            dimSet.add(dimension);
        }
        createElements();
    }

    public Group(DimensionSet dimSet) {
        this.dimSet = dimSet;
        createElements();
    }

    public void addDimension(Dimension dimension) {
        dimSet.add(dimension);
    }

    public void createElements() {
        elements = dimSet.getCombinations();
    }

    public String toString() {
        return "elements: " + elements.toString() + " dimSet: " + dimSet.toString();
    }

    public DimensionSet getElements() {
        return elements;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public String getURI() {
        return uri;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}