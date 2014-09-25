package com.mycsense.carbondb.domain;

import com.mycsense.carbondb.domain.group.Type;

public class Group
{
    public DimensionSet dimSet;
    public DimensionSet dimSetWithCommonKeywords;
    public DimensionSet elements;
    public Dimension commonKeywords;
    public Type type = Type.PROCESS;

    protected String label;
    protected String uri;
    protected String id;
    protected String unit;
    protected String unitURI;

    public Group() {
        dimSet = new DimensionSet();
        dimSetWithCommonKeywords = new DimensionSet();
        commonKeywords = new Dimension();
        elements = new DimensionSet();
        unit = new String();
        unitURI = new String();
    }

    public Group(Dimension... dimensions) {
        dimSet = new DimensionSet();
        dimSetWithCommonKeywords = new DimensionSet();
        commonKeywords = new Dimension();
        unit = new String();
        unitURI = new String();
        for (Dimension dimension: dimensions) {
            addDimension(dimension);
        }
        createElements();
    }

    public Group(DimensionSet dimSet) {
        this.dimSet = dimSet;
        dimSetWithCommonKeywords = new DimensionSet(dimSet);
        commonKeywords = new Dimension();
        unit = new String();
        unitURI = new String();
        createElements();
    }

    public Group(DimensionSet dimSet, Dimension commonKeywords) {
        this.dimSet = dimSet;
        dimSetWithCommonKeywords = new DimensionSet(dimSet);
        unit = new String();
        unitURI = new String();
        setCommonKeywords(commonKeywords);
        createElements();
    }

    public void addDimension(Dimension dimension) {
        dimSet.add(dimension);
        dimSetWithCommonKeywords.add(dimension);
    }

    public void addCommonKeyword(Keyword keyword) {
        commonKeywords.add(keyword);
        dimSetWithCommonKeywords.add(new Dimension(keyword));
    }

    public void setCommonKeywords(Dimension commonKeywords) {
        this.commonKeywords = new Dimension();
        for (Keyword keyword: commonKeywords.keywords) {
            addCommonKeyword(keyword);
        }
    }

    public void createElements() {
        elements = dimSetWithCommonKeywords.combinations();
    }

    public String toString() {
        return "elements: " + elements.toString() + " dimSet: " + dimSet.toString() + " unit: " + unit;
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

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnitURI(String unitURI) {
        this.unitURI = unitURI;
    }

    public String getUnitURI() {
        return unitURI;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}