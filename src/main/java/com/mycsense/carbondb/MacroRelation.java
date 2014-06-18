package com.mycsense.carbondb; 

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.StringBuilder;
import java.util.Collections;

import com.hp.hpl.jena.rdf.model.Resource;

class MacroRelation {
    Group source;
    Group coeff;
    Group destination;

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
        System.out.println("+++ translate called +++");
        ArrayList<MicroRelation> microRelations = new ArrayList<MicroRelation>();
        DimensionSet.UnionResult unionResult = source.dimSet.union(coeff.dimSet);
        Integer alpha1 = unionResult.alpha;
        Integer alpha2 = unionResult.dimSet.alpha(destination.dimSet);
        HashMap<String, ArrayList<Dimension>> coeffs = createGroupHashTable(coeff, unionResult.commonKeywords, alpha1);
        System.out.println(coeff);
        System.out.println(unionResult.commonKeywords);
        System.out.println(alpha1);
        System.out.println(coeffs);
        Dimension commonKeywordsGp1GcGp2 = unionResult.dimSet.getCommonKeywords(destination.dimSet);
        HashMap<String, ArrayList<Dimension>> destinationProcesses = createGroupHashTable(destination, commonKeywordsGp1GcGp2, alpha2);
        System.out.println("destinationProcesses = " + destinationProcesses + " commonKeywordsGp1GcGp2 = " + commonKeywordsGp1GcGp2 + "alpha2 = " + alpha2);

        for (Dimension sourceProcess: source.elements.dimensions) {
            String hashKey = getHashKey(sourceProcess, unionResult.commonKeywords, alpha1);
            if (!hashKey.equals("#nullHashKey#")) {
                System.out.println("hashKey = " + hashKey);
                for (Dimension coeff: coeffs.get(hashKey)) {
                    Dimension sourceAndCoeffKeywords = new Dimension(sourceProcess);
                    sourceAndCoeffKeywords.keywords.addAll(coeff.keywords);
                    String hashKey2 = getHashKey(sourceAndCoeffKeywords, commonKeywordsGp1GcGp2, alpha2);
                    if (!hashKey2.equals("#nullHashKey#")) {
                        for (Dimension destinationProcess: destinationProcesses.get(hashKey2)) {
                            microRelations.add(new MicroRelation(sourceProcess, coeff, destinationProcess));
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
        for (Resource keyword: dimension.keywords) {
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
}