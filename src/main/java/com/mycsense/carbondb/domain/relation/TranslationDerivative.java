package com.mycsense.carbondb.domain.relation;

import com.mycsense.carbondb.NoElementFoundException;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.Process;
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
            throws NoElementFoundException
    {
        com.mycsense.carbondb.domain.Process source, destination;
        Coefficient coeff = CarbonOntology.getInstance().findCoefficient(coeffKeywords, sourceRelation.getCoeff().getUnit());
        try  {
            source = CarbonOntology.getInstance().findProcess(sourceKeywords, sourceRelation.getSource().getUnit());
        }
        catch (NoElementFoundException e) {
            source = new Process(sourceKeywords, sourceRelation.getSource().getUnit());
        }
        try  {
            destination = CarbonOntology.getInstance().findProcess(destinationKeywords, sourceRelation.getDestination().getUnit());
        }
        catch (NoElementFoundException e) {
            destination = new Process(destinationKeywords, sourceRelation.getDestination().getUnit());
        }
        DerivedRelation derivedRelation = new DerivedRelation(
                source,
                coeff,
                destination,
                sourceRelation,
                sourceRelation.getType(),
                sourceRelation.getExponent());
        sourceRelation.addDerivedRelation(derivedRelation);
        return derivedRelation;
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
