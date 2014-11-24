package com.mycsense.carbondb.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Keyword implements Comparable<Keyword>
{
    protected String id;
    protected String label;

    public Keyword() {
        id = "";
    }

    public Keyword(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Keyword))
            return false;
        if (obj == this)
            return true;

        Keyword rhs = (Keyword) obj;
        return new EqualsBuilder()
                  .append(id, rhs.getId())
                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 79)
                  .append(id)
                  .toHashCode();
    }

    @Override
    public int compareTo(Keyword k) {
        return id.compareTo(k.id);
    }
}