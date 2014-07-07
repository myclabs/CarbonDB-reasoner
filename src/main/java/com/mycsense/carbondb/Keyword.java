package com.mycsense.carbondb;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Keyword
{
    public String name;

    protected String label;

    public Keyword() {
        name = new String();
    }

    public Keyword(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Keyword))
            return false;
        if (obj == this)
            return true;

        Keyword rhs = (Keyword) obj;
        return new EqualsBuilder()
                  .append(name, rhs.getName())
                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 79)
                  .append(name)
                  .toHashCode();
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}