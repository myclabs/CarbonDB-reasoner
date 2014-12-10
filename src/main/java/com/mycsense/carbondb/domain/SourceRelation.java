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

import com.mycsense.carbondb.IncompatibleDimSetException;
import com.mycsense.carbondb.IncompatibleUnitsException;
import com.mycsense.carbondb.domain.relation.TranslationDerivative;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.StringBuilder;
import java.util.Collections;

public class SourceRelation {
    protected Group source;
    protected Group coeff;
    protected Group destination;

    protected String id;
    protected int exponent = 1;
    protected RelationType type;
    protected ArrayList<DerivedRelation> derivedRelations;

    public SourceRelation(Group source, Group coeff, Group destination) {
        this.source = source;
        source.addSourceRelation(this);
        this.coeff = coeff;
        coeff.addSourceRelation(this);
        this.destination = destination;
        destination.addSourceRelation(this);
        derivedRelations = new ArrayList<>();
    }

    public String toString() {
        return source + " x " + coeff + " -> " + destination;
    }

    public ArrayList<TranslationDerivative> translate()
        throws IncompatibleDimSetException, IncompatibleUnitsException
    {
        if (!source.getFullDimSet().isCompatible(coeff.getFullDimSet())) {
            throw new IncompatibleDimSetException("The source group and the coeff group are incompatible "
                                                  + "in the source relation: " + id);
        }
        if (!source.getFullDimSet().isCompatible(destination.getFullDimSet())) {
            throw new IncompatibleDimSetException("The source group and the destination group are incompatible "
                                                  + "in the source relation: " + id);
        }
        if (!coeff.getFullDimSet().isCompatible(destination.getFullDimSet())) {
            throw new IncompatibleDimSetException("The coeff group and the destination group are incompatible "
                                                  + "in the source relation: " + id);
        }
        // The multiplication should be source.getUnit * coeff.getUnit isCompatibleWith destination.getUnit
        // but the (groups of) processes units are at power (-1), thus the easiest way to test for units
        // compatibility is with the following operation:
        if (!source.getUnit().isCompatible(destination.getUnit().multiply(coeff.getUnit(), exponent))) {
            throw new IncompatibleUnitsException("The units are incompatible "
                                                 + "in the source relation: " + id);
        }
        ArrayList<TranslationDerivative> translationDerivative = new ArrayList<>();
        DimensionSet.UnionResult unionResult = source.getFullDimSet().union(coeff.getFullDimSet());
        if (!unionResult.dimSet.isCompatible(destination.getFullDimSet())) {
            throw new IncompatibleDimSetException("The union of the source with the coeff groups and the destination "
                                                  + "group are incompatible in the source relation " + id);
        }
        Integer alpha1 = unionResult.alpha;
        Integer alpha2 = unionResult.dimSet.alpha(destination.getFullDimSet());
        HashMap<String, ArrayList<Dimension>> coeffs = createGroupHashTable(coeff, unionResult.commonKeywords, alpha1);

        Dimension commonKeywordsGp1GcGp2 = unionResult.dimSet.commonKeywords(destination.getFullDimSet());
        HashMap<String, ArrayList<Dimension>> destinationProcesses = createGroupHashTable(destination, commonKeywordsGp1GcGp2, alpha2);
        String hashKey2;
        for (Dimension sourceProcess: source.getCoordinates().dimensions) {
            String hashKey = getHashKey(sourceProcess, unionResult.commonKeywords, alpha1);
            if (!hashKey.equals("#nullHashKey#")) {
                for (Dimension singleCoeff: coeffs.get(hashKey)) {
                    Dimension sourceAndCoeffKeywords = new Dimension(sourceProcess);
                    sourceAndCoeffKeywords.keywords.addAll(singleCoeff.keywords);
                    hashKey2 = getHashKey(sourceAndCoeffKeywords, commonKeywordsGp1GcGp2, alpha2);
                    if (!hashKey2.equals("#nullHashKey#")) {
                        for (Dimension destinationProcess: destinationProcesses.get(hashKey2)) {
                            translationDerivative.add(new TranslationDerivative(
                                    sourceProcess,
                                    singleCoeff,
                                    destinationProcess,
                                    this));
                        }
                    }
                }
            }
        }

        return translationDerivative;
    }

    public static String getHashKey(Dimension dimension, Dimension commonKeywords, Integer alpha)
    {
        if (alpha == 0) {
            return "#emptyHashKey#";
        }

        ArrayList<String> keywordInKey = new ArrayList<>();
        for (Keyword keyword: dimension.keywords) {
            if (commonKeywords.contains(keyword)) {
                keywordInKey.add(keyword.toString());
            }
        }
        if (keywordInKey.size() != alpha) {
            return "#nullHashKey#";
        }

        Collections.sort(keywordInKey);
        return implode(",", keywordInKey.toArray(new String[keywordInKey.size()]));
    }

    public static HashMap<String, ArrayList<Dimension>> createGroupHashTable(Group group, Dimension commonKeywords, Integer alpha)
    {
        HashMap<String, ArrayList<Dimension>> elements = new HashMap<>();
        for (Dimension element: group.getCoordinates().dimensions) {
            String hashKey = getHashKey(element, commonKeywords, alpha);
            if (!hashKey.equals("#nullHashKey#")) {
                if (!elements.containsKey(hashKey)) {
                    elements.put(hashKey, new ArrayList<Dimension>());
                }
                elements.get(hashKey).add(element);
            }
        }
        return elements;
    }

    protected static String implode(String separator, String... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length - 1; i++) {
            sb.append(data[i]);
            sb.append(separator);
        }
        sb.append(data[data.length - 1]);
        return sb.toString();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setExponent(int exponent) {
        this.exponent = exponent;
    }

    public int getExponent() {
        return exponent;
    }

    public RelationType getType() {
        return type;
    }

    public void setType(RelationType type) {
        this.type = type;
    }

    public ArrayList<DerivedRelation> getDerivedRelations() {
        return derivedRelations;
    }

    public void setDerivedRelations(ArrayList<DerivedRelation> derivedRelations) {
        this.derivedRelations = derivedRelations;
    }

    public void addDerivedRelation(DerivedRelation derivedRelation) {
        derivedRelations.add(derivedRelation);
    }

    public Group getSource() {
        return source;
    }

    public Group getCoeff() {
        return coeff;
    }

    public Group getDestination() {
        return destination;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Group))
            return false;
        if (obj == this)
            return true;

        Group rhs = (Group) obj;
        return new EqualsBuilder()
                .append(id, rhs.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(229, 881)
                .append(id)
                .toHashCode();
    }
}