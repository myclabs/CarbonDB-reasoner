package com.mycsense.carbondb.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DerivedRelation {
    public Dimension source;
    public String sourceURI;
    public Unit sourceUnit;
    public Dimension coeff;
    public String coeffURI;
    public Unit coeffUnit;
    public Dimension destination;
    public String destinationURI;
    public Unit destinationUnit;
    public int exponent;

    public DerivedRelation(Dimension source,
                           Unit sourceUnit,
                           Dimension coeff,
                           Unit coeffUnit,
                           Dimension destination,
                           Unit destinationUnit
    ) {
        this(source, sourceUnit, coeff, coeffUnit, destination, destinationUnit, 1);
    }

    public DerivedRelation(Dimension source,
                           Unit sourceUnit,
                           Dimension coeff,
                           Unit coeffUnit,
                           Dimension destination,
                           Unit destinationUnit,
                           int exponent
    ) {
        this.source = source;
        this.sourceUnit = sourceUnit;
        this.coeff = coeff;
        this.coeffUnit = coeffUnit;
        this.destination = destination;
        this.destinationUnit = destinationUnit;
        this.exponent = exponent;
    }

    public String toString() {
        return source + " ( " + sourceUnit + ")" + " x " + coeff + " ( " + coeffUnit + ")" + " -> " + destination + " ( " + destinationUnit + ") ^" + exponent;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DerivedRelation))
            return false;
        if (obj == this)
            return true;

        DerivedRelation rhs = (DerivedRelation) obj;
        return new EqualsBuilder()
                  .append(source, rhs.source)
                  .append(sourceUnit, rhs.sourceUnit)
                  .append(coeff, rhs.coeff)
                  .append(coeffUnit, rhs.coeffUnit)
                  .append(destination, rhs.destination)
                  .append(destinationUnit, rhs.destinationUnit)
                  .append(exponent, rhs.exponent)
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
                  .append(exponent)
                  .toHashCode();
    }
}