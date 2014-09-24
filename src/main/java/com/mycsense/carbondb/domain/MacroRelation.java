package com.mycsense.carbondb.domain;

import com.mycsense.carbondb.IncompatibleDimSetException;
import com.mycsense.carbondb.IncompatibleUnitsException;
import com.mycsense.carbondb.architecture.UnitsRepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.StringBuilder;
import java.util.Collections;

public class MacroRelation {
    public Group source;
    public Group coeff;
    public Group destination;

    protected String uri;
    protected int exponent = 1;
    protected UnitsRepo unitsRepo;

    public MacroRelation(Group source, Group coeff, Group destination, UnitsRepo unitsRepo) {
        this.source = source;
        this.coeff = coeff;
        this.destination = destination;
        this.unitsRepo = unitsRepo;
    }

    public String toString() {
        return source + " x " + coeff + " -> " + destination;
    }

    public ArrayList<MicroRelation> translate()
        throws IncompatibleDimSetException, IncompatibleUnitsException
    {
        if (!source.dimSetWithCommonKeywords.isCompatible(coeff.dimSetWithCommonKeywords)) {
            throw new IncompatibleDimSetException("The source group and the coeff group are incompatible "
                                                  + "in the macro relation: " + uri);
        }
        if (!source.dimSetWithCommonKeywords.isCompatible(destination.dimSetWithCommonKeywords)) {
            throw new IncompatibleDimSetException("The source group and the destination group are incompatible "
                                                  + "in the macro relation: " + uri);
        }
        if (!coeff.dimSetWithCommonKeywords.isCompatible(destination.dimSetWithCommonKeywords)) {
            throw new IncompatibleDimSetException("The coeff group and the destination group are incompatible "
                                                  + "in the macro relation: " + uri);
        }
        if (!unitsRepo.areCompatible(source.getUnit(), unitsRepo.getUnitsMultiplication(destination.getUnit(), coeff.getUnit(), exponent))) {
            throw new IncompatibleUnitsException("The units are incompatible "
                                                 + "in the macro relation: " + uri);
        }
        ArrayList<MicroRelation> microRelations = new ArrayList<>();
        DimensionSet.UnionResult unionResult = source.dimSetWithCommonKeywords.union(coeff.dimSetWithCommonKeywords);
        if (!unionResult.dimSet.isCompatible(destination.dimSetWithCommonKeywords)) {
            throw new IncompatibleDimSetException("The union of the source with the coeff groups and the destination "
                                                  + "group are incompatible in the macro relation " + uri);
        }
        Integer alpha1 = unionResult.alpha;
        Integer alpha2 = unionResult.dimSet.alpha(destination.dimSetWithCommonKeywords);
        HashMap<String, ArrayList<Dimension>> coeffs = createGroupHashTable(coeff, unionResult.commonKeywords, alpha1);

        Dimension commonKeywordsGp1GcGp2 = unionResult.dimSet.getCommonKeywords(destination.dimSetWithCommonKeywords);
        HashMap<String, ArrayList<Dimension>> destinationProcesses = createGroupHashTable(destination, commonKeywordsGp1GcGp2, alpha2);
        String hashKey2;
        for (Dimension sourceProcess: source.elements.dimensions) {
            String hashKey = getHashKey(sourceProcess, unionResult.commonKeywords, alpha1);
            if (!hashKey.equals("#nullHashKey#")) {
                for (Dimension singleCoeff: coeffs.get(hashKey)) {
                    Dimension sourceAndCoeffKeywords = new Dimension(sourceProcess);
                    sourceAndCoeffKeywords.keywords.addAll(singleCoeff.keywords);
                    hashKey2 = getHashKey(sourceAndCoeffKeywords, commonKeywordsGp1GcGp2, alpha2);
                    if (!hashKey2.equals("#nullHashKey#")) {
                        for (Dimension destinationProcess: destinationProcesses.get(hashKey2)) {
                            microRelations.add(new MicroRelation(sourceProcess,
                                                                 source.getUnitURI(),
                                                                 singleCoeff,
                                                                 coeff.getUnitURI(),
                                                                 destinationProcess,
                                                                 destination.getUnitURI(),
                                                                 exponent));
                        }
                    }
                }
            }
        }

        return microRelations;
    }

    protected static String getHashKey(Dimension dimension, Dimension commonKeywords, Integer alpha)
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

    protected static HashMap<String, ArrayList<Dimension>> createGroupHashTable(Group group, Dimension commonKeywords, Integer alpha)
    {
        HashMap<String, ArrayList<Dimension>> elements = new HashMap<>();
        for (Dimension element: group.elements.dimensions) {
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

    public void setURI(String uri) {
        this.uri = uri;
    }

    public String getURI() {
        return uri;
    }

    public void setExponent(int exponent) {
        this.exponent = exponent;
    }

    public int getExponent() {
        return exponent;
    }
}