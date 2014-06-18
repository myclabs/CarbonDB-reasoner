package com.mycsense.carbondb; 

class Group
{
    DimensionSet dimSet;
    DimensionSet elements;

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
}