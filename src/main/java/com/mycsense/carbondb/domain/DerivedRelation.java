package com.mycsense.carbondb.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DerivedRelation {
    protected Process source;
    protected Coefficient coeff;
    protected Process destination;
    protected int exponent;
    protected SourceRelation sourceRelation;
    protected RelationType type;

    public DerivedRelation(Process source,
                           Coefficient coeff,
                           Process destination,
                           SourceRelation sourceRelation,
                           RelationType type
    ) {
        this(source, coeff, destination, sourceRelation, type, 1);
    }

    public DerivedRelation(Process source,
                           Coefficient coeff,
                           Process destination,
                           SourceRelation sourceRelation,
                           RelationType type,
                           int exponent
    ) {
        this.source = source;
        this.coeff = coeff;
        this.destination = destination;
        this.sourceRelation = sourceRelation;
        this.type = type;
        this.exponent = exponent;

        source.addDownstreamDerivedRelation(this);
        destination.addUpstreamDerivedRelation(this);
        coeff.addDerivedRelation(this);
        sourceRelation.addDerivedRelation(this);
    }

    public String toString() {
        return source + " x " + coeff + " -> " + destination + " ^" + exponent;
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
                  .append(coeff, rhs.coeff)
                  .append(destination, rhs.destination)
                  .append(exponent, rhs.exponent)
                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 47)
                  .append(source)
                  .append(coeff)
                  .append(destination)
                  .append(exponent)
                  .toHashCode();
    }

    public Process getSource() {
        return source;
    }

    public Coefficient getCoeff() {
        return coeff;
    }

    public Process getDestination() {
        return destination;
    }

    public int getExponent() {
        return exponent;
    }

    public SourceRelation getSourceRelation() {
        return sourceRelation;
    }

    public RelationType getType() {
        return type;
    }
}