package com.mycsense.carbondb; 

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class MicroRelation {
    Dimension source;
    Dimension coeff;
    Dimension destination;

    public MicroRelation(Dimension source, Dimension coeff, Dimension destination) {
        this.source = source;
        this.coeff = coeff;
        this.destination = destination;
    }

    public String toString() {
        return source + " x " + coeff + " -> " + destination;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MicroRelation))
            return false;
        if (obj == this)
            return true;

        MicroRelation rhs = (MicroRelation) obj;
        return new EqualsBuilder()
                  .append(source, rhs.source)
                  .append(coeff, rhs.coeff)
                  .append(destination, rhs.destination)
                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 47)
                  .append(source)
                  .append(coeff)
                  .append(destination)
                  .toHashCode();
    }
}