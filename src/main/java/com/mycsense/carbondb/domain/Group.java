package com.mycsense.carbondb.domain;

import com.mycsense.carbondb.NoElementFoundException;
import com.mycsense.carbondb.domain.group.Type;

import java.util.ArrayList;
import java.util.HashSet;

public class Group
{
    protected DimensionSet dimSet;
    /**
     * Contains the dimension set and the common keywords
     */
    protected DimensionSet fullDimSet;
    protected DimensionSet coordinates;
    protected Dimension commonKeywords;
    protected Type type = Type.PROCESS;
    protected HashSet<SingleElement> elements;

    protected String label;
    protected String id;
    protected Unit unit;
    protected String comment;

    protected ArrayList<Reference> references;

    public Group() {
        this(new DimensionSet());
    }

    public Group(Dimension... dimensions) {
        this(new DimensionSet(dimensions));
    }

    public Group(DimensionSet dimSet) {
        this(dimSet, new Dimension());
    }

    public Group(DimensionSet dimSet, Dimension commonKeywords) {
        this.dimSet = dimSet;
        fullDimSet = new DimensionSet(dimSet);
        setCommonKeywords(commonKeywords);
        createCoordinates();
        fetchElements();
    }

    public void addDimension(Dimension dimension) {
        dimSet.add(dimension);
        fullDimSet.add(dimension);
    }

    public void addCommonKeyword(Keyword keyword) {
        commonKeywords.add(keyword);
        fullDimSet.add(new Dimension(keyword));
    }

    public void setCommonKeywords(Dimension commonKeywords) {
        this.commonKeywords = new Dimension();
        for (Keyword keyword: commonKeywords.keywords) {
            addCommonKeyword(keyword);
        }
    }

    public void createCoordinates() {
        coordinates = fullDimSet.combinations();
    }

    public String toString() {
        return "coordinates: " + coordinates.toString() + " dimSet: " + dimSet.toString() + " unit: " + unit;
    }

    public DimensionSet getCoordinates() {
        return coordinates;
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

    public DimensionSet getFullDimSet() {
        return fullDimSet;
    }

    public HashSet<SingleElement> getElements() {
        return elements;
    }

    public void addElement(SingleElement element) {
        elements.add(element);
        element.addGroup(this);
    }

    public void fetchElements() {
        elements = new HashSet<>();
        for (Dimension coordinate : coordinates.dimensions) {
            try {
                if (type == Type.COEFFICIENT)
                    addElement(CarbonOntology.getInstance().findCoefficient(coordinate, unit));

                else
                    addElement(CarbonOntology.getInstance().findProcess(coordinate, unit));
            } catch (NoElementFoundException e) {
                // nothing to do here, there is no element at this coordinate
            }
        }
    }

    public ArrayList<Reference> getReferences() {
        return references;
    }

    public void setReferences(ArrayList<Reference> references) {
        this.references = references;
    }
}