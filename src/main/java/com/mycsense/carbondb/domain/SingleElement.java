package com.mycsense.carbondb.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class SingleElement {
    public Dimension keywords;

    protected String uri;
    protected String id;
    protected Unit unit;

    public SingleElement() {
        keywords = new Dimension();
        uri = new String();
        id = new String();
    }

    public SingleElement(Dimension keywords, Unit unit) {
        this.keywords = new Dimension(keywords);
        this.unit = unit;
        uri = new String();
        id = new String();
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String toString() {
        return keywords + " ("+ unit.getSymbol() +" )";
    }

    @Override
    public boolean equals(Object obj) {
        // Alternative: use Guava (from Google)
        if (!(obj instanceof SingleElement))
            return false;
        if (obj == this)
            return true;

        SingleElement rhs = (SingleElement) obj;
        return new EqualsBuilder()
                .append(keywords, rhs.keywords)
                .append(unit, rhs.unit)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 67)
                .append(keywords)
                .append(unit)
                .toHashCode();
    }
}
