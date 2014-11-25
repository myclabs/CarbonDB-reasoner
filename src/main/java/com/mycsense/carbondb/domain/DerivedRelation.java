/*
 * Copyright 2014, by Benjamin Bertin and Contributors.
 *
 * This file is part of CarbonDB-reasoner project <http://www.carbondb.org>
 *
 * CarbonDB-reasoner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * CarbonDB-reasoner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CarbonDB-reasoner.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributor(s): -
 *
 */

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