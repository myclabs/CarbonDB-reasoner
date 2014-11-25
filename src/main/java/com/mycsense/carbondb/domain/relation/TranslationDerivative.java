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

package com.mycsense.carbondb.domain.relation;

import com.mycsense.carbondb.AlreadyExistsException;
import com.mycsense.carbondb.NoElementFoundException;
import com.mycsense.carbondb.domain.CarbonOntology;
import com.mycsense.carbondb.domain.Coefficient;
import com.mycsense.carbondb.domain.DerivedRelation;
import com.mycsense.carbondb.domain.Dimension;
import com.mycsense.carbondb.domain.Process;
import com.mycsense.carbondb.domain.SourceRelation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class TranslationDerivative {
    protected Dimension sourceKeywords, coeffKeywords, destinationKeywords;
    protected SourceRelation sourceRelation;

    public TranslationDerivative(Dimension sourceKeywords,
                                 Dimension coeffKeywords,
                                 Dimension destinationKeywords,
                                 SourceRelation sourceRelation
    ) {
        this.sourceKeywords = sourceKeywords;
        this.coeffKeywords = coeffKeywords;
        this.destinationKeywords = destinationKeywords;
        this.sourceRelation = sourceRelation;
    }

    public DerivedRelation transformToDerivedRelation()
            throws NoElementFoundException, AlreadyExistsException {
        com.mycsense.carbondb.domain.Process source, destination;
        Coefficient coeff = CarbonOntology.getInstance().findCoefficient(coeffKeywords, sourceRelation.getCoeff().getUnit());
        try  {
            source = CarbonOntology.getInstance().findProcess(sourceKeywords, sourceRelation.getSource().getUnit());
        }
        catch (NoElementFoundException e) {
            source = new Process(sourceKeywords, sourceRelation.getSource().getUnit());
            sourceRelation.getSource().addElement(source);
            CarbonOntology.getInstance().addProcess(source);
        }
        try  {
            destination = CarbonOntology.getInstance().findProcess(destinationKeywords, sourceRelation.getDestination().getUnit());
        }
        catch (NoElementFoundException e) {
            destination = new Process(destinationKeywords, sourceRelation.getDestination().getUnit());
            sourceRelation.getDestination().addElement(destination);
            CarbonOntology.getInstance().addProcess(destination);
        }
        return new DerivedRelation(
                source,
                coeff,
                destination,
                sourceRelation,
                sourceRelation.getType(),
                sourceRelation.getExponent());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TranslationDerivative))
            return false;
        if (obj == this)
            return true;

        TranslationDerivative rhs = (TranslationDerivative) obj;
        return new EqualsBuilder()
                .append(sourceKeywords, rhs.sourceKeywords)
                .append(coeffKeywords, rhs.coeffKeywords)
                .append(destinationKeywords, rhs.destinationKeywords)
                .append(sourceRelation, rhs.sourceRelation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(113, 431)
                .append(sourceKeywords)
                .append(coeffKeywords)
                .append(destinationKeywords)
                .append(sourceRelation)
                .toHashCode();
    }
}
