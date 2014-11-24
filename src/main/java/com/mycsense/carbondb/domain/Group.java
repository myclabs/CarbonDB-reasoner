package com.mycsense.carbondb.domain;

import com.mycsense.carbondb.domain.group.Type;

import java.util.ArrayList;

public class Group
{
    public DimensionSet dimSet;
    public DimensionSet dimSetWithCommonKeywords;
    public DimensionSet elements;
    public Dimension commonKeywords;
    public Type type = Type.PROCESS;

    protected String label;
    protected String id;
    protected Unit unit;
    protected String comment;

    protected ArrayList<Reference> references;

    public Group() {
        dimSet = new DimensionSet();
        dimSetWithCommonKeywords = new DimensionSet();
        commonKeywords = new Dimension();
        elements = new DimensionSet();
    }

    public Group(Dimension... dimensions) {
        dimSet = new DimensionSet();
        dimSetWithCommonKeywords = new DimensionSet();
        commonKeywords = new Dimension();
        for (Dimension dimension: dimensions) {
            addDimension(dimension);
        }
        createElements();
    }

    public Group(DimensionSet dimSet) {
        this.dimSet = dimSet;
        dimSetWithCommonKeywords = new DimensionSet(dimSet);
        commonKeywords = new Dimension();
        createElements();
    }

    public Group(DimensionSet dimSet, Dimension commonKeywords) {
        this.dimSet = dimSet;
        dimSetWithCommonKeywords = new DimensionSet(dimSet);
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

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ArrayList<Reference> getReferences() {
        return references;
    }

    public void setReferences(ArrayList<Reference> references) {
        this.references = references;
    }
}