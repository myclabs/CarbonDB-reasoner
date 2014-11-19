package com.mycsense.carbondb.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class SingleElement {
    public Dimension keywords;

    protected String uri;
    protected String id;
    protected String unit;
    protected String unitURI;

    public SingleElement() {
        keywords = new Dimension();
        unit = new String();
        unitURI = new String();
        uri = new String();
        id = new String();
    }

    public SingleElement(Dimension keywords) {
        this.keywords = new Dimension(keywords);
        unit = new String();
        unitURI = new String();
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitURI() {
        return unitURI;
    }

    public void setUnitURI(String unitURI) {
        this.unitURI = unitURI;
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
