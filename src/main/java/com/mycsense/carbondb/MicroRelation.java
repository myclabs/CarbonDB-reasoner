package com.mycsense.carbondb; 

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class MicroRelation {
    Dimension source;
    String sourceUnit;
    Dimension coeff;
    String coeffUnit;
    Dimension destination;
    String destinationUnit;

    public MicroRelation(Dimension source,
                         String sourceUnit,
                         Dimension coeff,
                         String coeffUnit,
                         Dimension destination,
                         String destinationUnit
    ) {
        this.source = source;
        this.sourceUnit = sourceUnit;
        this.coeff = coeff;
        this.coeffUnit = coeffUnit;
        this.destination = destination;
        this.destinationUnit = destinationUnit;
    }

    public String toString() {
        return source + " ( " + sourceUnit + ")" + " x " + coeff + " ( " + coeffUnit + ")" + " -> " + destination + " ( " + destinationUnit + ")";
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
                  .append(sourceUnit, rhs.sourceUnit)
                  .append(coeff, rhs.coeff)
                  .append(coeffUnit, rhs.coeffUnit)
                  .append(destination, rhs.destination)
                  .append(destinationUnit, rhs.destinationUnit)
                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 47)
                  .append(source)
                  .append(sourceUnit)
                  .append(coeff)
                  .append(coeffUnit)
                  .append(destination)
                  .append(destinationUnit)
                  .toHashCode();
    }
}