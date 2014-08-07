package com.mycsense.carbondb; 

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.StringBuilder;
import java.util.Collections;

class MacroRelation {
    public Group source;
    public Group coeff;
    public Group destination;

    protected String uri;

    public MacroRelation(Group source, Group coeff, Group destination) {
        this.source = source;
        this.coeff = coeff;
        this.destination = destination;
    }

    public String toString() {
        return source + " x " + coeff + " -> " + destination;
    }

    public ArrayList<MicroRelation> translate()
    {
        ArrayList<MicroRelation> microRelations = new ArrayList<MicroRelation>();
        DimensionSet.UnionResult unionResult = source.dimSetWithCommonKeywords.union(coeff.dimSetWithCommonKeywords);
        Integer alpha1 = unionResult.alpha;
        Integer alpha2 = unionResult.dimSet.alpha(destination.dimSetWithCommonKeywords);
        HashMap<String, ArrayList<Dimension>> coeffs = createGroupHashTable(coeff, unionResult.commonKeywords, alpha1);

        Dimension commonKeywordsGp1GcGp2 = unionResult.dimSet.getCommonKeywords(destination.dimSetWithCommonKeywords);
        HashMap<String, ArrayList<Dimension>> destinationProcesses = createGroupHashTable(destination, commonKeywordsGp1GcGp2, alpha2);

        for (Dimension sourceProcess: source.elements.dimensions) {
            String hashKey = getHashKey(sourceProcess, unionResult.commonKeywords, alpha1);
            if (!hashKey.equals("#nullHashKey#")) {
                for (Dimension singleCoeff: coeffs.get(hashKey)) {
                    Dimension sourceAndCoeffKeywords = new Dimension(sourceProcess);
                    sourceAndCoeffKeywords.keywords.addAll(singleCoeff.keywords);
                    String hashKey2 = getHashKey(sourceAndCoeffKeywords, commonKeywordsGp1GcGp2, alpha2);
                    if (!hashKey2.equals("#nullHashKey#")) {
                        for (Dimension destinationProcess: destinationProcesses.get(hashKey2)) {
                            microRelations.add(new MicroRelation(sourceProcess, source.getUnitURI(), singleCoeff, coeff.getUnitURI(), destinationProcess, destination.getUnitURI()));
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

        ArrayList<String> keywordInKey = new ArrayList<String>();
        for (Keyword keyword: dimension.keywords) {
            if (commonKeywords.contains(keyword)) {
                keywordInKey.add(keyword.toString());
            }
        }
        if (keywordInKey.size() != alpha) {
            return "#nullHashKey#";
        }

        Collections.sort(keywordInKey);
        return implode(",", keywordInKey.toArray(new String[0]));
    }

    protected static HashMap<String, ArrayList<Dimension>> createGroupHashTable(Group group, Dimension commonKeywords, Integer alpha)
    {
        HashMap<String, ArrayList<Dimension>> elements = new HashMap<String, ArrayList<Dimension>>();
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
}