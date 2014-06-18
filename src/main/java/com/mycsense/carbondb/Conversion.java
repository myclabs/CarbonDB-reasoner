package com.mycsense.carbondb; 

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import java.io.*;
import java.io.FileOutputStream;
import com.hp.hpl.jena.util.FileManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.reasoner.*;
import java.lang.StringBuilder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Conversion {

/**
TODO: transform this class to remove the dependency to jena (add a class that will load the model)
      Resource -> Keyword, move the creation of ontology elements in another class
      this class should only use a class Model containing groups, microRelations and macroRelations
**/

    //protected Model model;
    public Model model;
    protected InfModel infModel;
    protected ArrayList<Resource> processes;
    protected ArrayList<Resource> elementaryFlowNatures;

    Conversion (Model model) {
        this.model = model;
    }

    public void run() {
        for (MacroRelation macroRelation: getMacroRelations()) {
            createMicroRelations(translate(macroRelation));
        }
    }

    public ArrayList<MacroRelation> getMacroRelations() {
        ArrayList<MacroRelation> macroRelations = new ArrayList<MacroRelation>();

        Selector selector = new SimpleSelector(null, RDF.type, Datatype.Relation);
        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.Relation);
        while (i.hasNext()) {
            Resource macroRelation = i.next();
            macroRelations.add(new MacroRelation(
                getGroup(macroRelation.getProperty(Datatype.hasOrigin).getResource()),
                getGroup(macroRelation.getProperty(Datatype.hasWeight).getResource()),
                getGroup(macroRelation.getProperty(Datatype.hasDestination).getResource())
            ));
        }

        return macroRelations;
    }

    Conversion (String inputFileName) {

        model = ModelFactory.createDefaultModel();

        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException(
                        "File: " + inputFileName + " not found");
        }

        model.read(in, null);

        System.out.println("+++ RDF file readed +++");

        System.out.println(getFamilyDimSet(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")));
        System.out.println(alpha(
            getFamilyDimSet(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")),
            getFamilyDimSet(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp2"))
         ));
        System.out.println(union(
            getFamilyDimSet(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")),
            getFamilyDimSet(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp2"))
        ));
        Dimension commonKeywords = new Dimension();
        commonKeywords.add(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#kw1"));
        commonKeywords.add(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#kw2"));
        System.out.println(getHashKey(
            getDimensionKeywords(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#dim1")),
            commonKeywords,
            2
        ));
        //DimensionSet dimSetA = new DimensionSet();
        Dimension dimA1 = new Dimension();
        dimA1.add(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#kw1"));
        dimA1.add(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#kw2"));
        //dimSetA.add(dimA1);
        /*Dimension dimA2 = new Dimension();
        dimA2.add(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#kw3"));
        dimA2.add(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#kw4"));
        dimSetA.add(dimA2);*/
        //System.out.println(dimSetA.getCombinations());
        System.out.println("+++ group fp1 +++");
        System.out.println(getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")));
        System.out.println("+++ group fc +++");
        System.out.println(getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fc")));

        System.out.println(getProcessForDimension(dimA1));

        System.out.println(createGroupHashTable(getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")), dimA1, 1));

        System.out.println(
            getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")).dimSet.getCommonKeywords(
                getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp2")).dimSet
        ));

        System.out.println(translate(
            getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")),
            getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fc")),
            getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp2"))
        ));

        createMicroRelations(translate(
            getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")),
            getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fc")),
            getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp2"))
        ));

        /*try {
            FileOutputStream out = new FileOutputStream("ontologies/bc-test-inferred-java.rdf");
            model.write(out);
        }
        catch (IOException e) {
            System.out.println("error while writing the onto file");
        }*/
    }

    protected Resource getProcessForDimension(Dimension dimension)
    {
        return getElementForDimension(dimension, "process");
    }

    protected Resource getCoefficientForDimension(Dimension dimension)
    {
        return getElementForDimension(dimension, "coefficient");
    }

    protected Resource getElementForDimension(Dimension dimension, String type)
    {
        // @todo: throw an exception instead of returning null when the element could not be found?
        System.out.println("--- Process exists called ---");
        Resource single;
        if (type.equals("process")) {
            single = Datatype.SingleProcess;
        }
        else {
            single = Datatype.SingleCoefficient;
        }
        if (dimension.size() == 0) {
            return (Resource) null;
        }
        Iterator<Resource> iter = dimension.keywords.iterator();
        Resource keyword = iter.next();
        Selector selector = new SimpleSelector(null, Datatype.hasKeyword, keyword);
        ResIterator statementIter = model.listSubjectsWithProperty(Datatype.hasKeyword, keyword);
        //StmtIterator statementIter = model.listStatements( selector );
        List<Resource> candidates = statementIter.toList();
        System.out.println(candidates);
        Iterator<Resource> i = candidates.iterator();
        while (i.hasNext()) {
            Resource candidate = i.next();
            if (!model.contains(candidate, RDF.type, (RDFNode) single)) {
                i.remove();
            }
            else {
                NodeIterator nodeIter = model.listObjectsOfProperty(candidate, Datatype.hasKeyword);
                int listSize = nodeIter.toList().size();
                if (listSize != dimension.size()) {
                    System.out.println("removing candidate (" + candidate + "), kw size = " + listSize + " / dim size = " + dimension.size());
                    i.remove();
                }
            }
        }
        System.out.println(candidates);

        while (iter.hasNext()) {
            keyword = iter.next();
            i = candidates.iterator();
            while (i.hasNext()) {
                Resource candidate = i.next();
                if (!model.contains(candidate, Datatype.hasKeyword, (RDFNode) keyword)) {
                    i.remove();
                }
            }
        }
        System.out.println(candidates);
        if (candidates.isEmpty()) {
            return (Resource) null;
        }
        return candidates.get(0);
    }

    protected Group getGroup(Resource family)
    {
        Group group = new Group(getFamilyDimSet(family));
        return group;
    }

    protected String getHashKey(Dimension dimension, Dimension commonKeywords, Integer alpha)
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

    protected DimensionSet getFamilyDimSet(Resource family)
    {
        Selector selector = new SimpleSelector(family, Datatype.hasDimension, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        DimensionSet dimSet = new DimensionSet();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                dimSet.add(getDimensionKeywords((Resource) s.getObject()));
            }
        }
        return dimSet;
    }

    protected Dimension getDimensionKeywords(Resource dimension)
    {
        Selector selector = new SimpleSelector(dimension, Datatype.hasKeyword, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        Dimension dim = new Dimension();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                dim.add((Resource) s.getObject());
            }
        }
        return dim;
    }

    protected Integer alpha(DimensionSet dimSetA, DimensionSet dimSetB)
    {
        Integer alpha = 0;
        Dimension hashTableB = new Dimension();
        for (Dimension dimension: dimSetB.dimensions) {
            for (Resource keyword: dimension.keywords) {
                hashTableB.add(keyword);
            }
        }
        for (Dimension dimension: dimSetA.dimensions) {
            if (dimension.hasCommonKeywords(hashTableB)) {
                alpha++;
            }
        }
        return alpha;
    }

    public UnionResult union(DimensionSet dimSetA, DimensionSet dimSetB)
    {
        UnionResult r = new UnionResult();

        r.dimSet = new DimensionSet();
        r.alpha = 0;
        r.commonKeywords = new Dimension();
        Integer dimIndex = -1;

        // HashTable for dimSetB keywords construction
        HashMap<Resource, Dimension> hashTableB = new HashMap<Resource, Dimension>();
        DimensionSet unusedDimsB = new DimensionSet();

        for (Dimension dimension: dimSetB.dimensions) {
            unusedDimsB.add(dimension);
            for (Resource keyword: dimension.keywords) {
                hashTableB.put(keyword, dimension);
            }
        }
        for (Dimension dimension: dimSetA.dimensions) {
            Dimension dimResultTemp = new Dimension();
            for (Resource keyword: dimension.keywords) {
                if (hashTableB.containsKey(keyword)) {
                    unusedDimsB.remove(hashTableB.get(keyword));
                    dimResultTemp.add(keyword);
                    r.commonKeywords.add(keyword);
                }
            }
            if (dimResultTemp.isEmpty()) {
                r.dimSet.add(dimension);
            }
            else {
                r.dimSet.add(dimResultTemp);
                r.alpha++;
            }
        }
        for (Dimension dimension: unusedDimsB.dimensions) {
            r.dimSet.add(dimension);
        }

        return r;
    }

    protected class UnionResult {
        DimensionSet dimSet;
        Integer alpha;
        Dimension commonKeywords;

        public String toString()
        {
            return dimSet.toString();
        }
    }

    public void createMicroRelations(ArrayList<MicroRelation> microRelations)
    {
        for (MicroRelation microRelation: microRelations) {
            Resource sourceProcess = getProcessForDimension(microRelation.source);
            Resource coeff = getCoefficientForDimension(microRelation.coeff);
            Resource destinationProcess = getProcessForDimension(microRelation.destination);
            if (sourceProcess != null && coeff != null && destinationProcess != null) {
                System.out.println("Creating micro-relation");
                sourceProcess.addProperty(Datatype.hasDetailedRelation,
                    model.createResource(Datatype.getURI() + AnonId.create().toString())
                    .addProperty(RDF.type, Datatype.Relation)
                    .addProperty(Datatype.hasOrigin, sourceProcess)
                    .addProperty(Datatype.hasWeight, coeff)
                    .addProperty(Datatype.hasDestination, destinationProcess));
            }
        }
    }

    public ArrayList<MicroRelation> translate(MacroRelation macroRelation)
    {
        return translate(macroRelation.source, macroRelation.coeff, macroRelation.destination);
    }

    public ArrayList<MicroRelation> translate(Group sourceProcessGroup, Group coeffGroup, Group destinationProcessGroup)
    {
        System.out.println("+++ translate called +++");
        ArrayList<MicroRelation> microRelations = new ArrayList<MicroRelation>();
        UnionResult unionResult = union(
            sourceProcessGroup.dimSet,
            coeffGroup.dimSet
        );
        Integer alpha1 = unionResult.alpha;
        Integer alpha2 = alpha(unionResult.dimSet, destinationProcessGroup.dimSet);
        HashMap<String, ArrayList<Dimension>> coeffs = createGroupHashTable(coeffGroup, unionResult.commonKeywords, alpha1);
        System.out.println(coeffGroup);
        System.out.println(unionResult.commonKeywords);
        System.out.println(alpha1);
        System.out.println(coeffs);
        Dimension commonKeywordsGp1GcGp2 = unionResult.dimSet.getCommonKeywords(destinationProcessGroup.dimSet);
        HashMap<String, ArrayList<Dimension>> destinationProcesses = createGroupHashTable(destinationProcessGroup, commonKeywordsGp1GcGp2, alpha2);
        System.out.println("destinationProcesses = " + destinationProcesses + " commonKeywordsGp1GcGp2 = " + commonKeywordsGp1GcGp2 + "alpha2 = " + alpha2);

        for (Dimension sourceProcess: sourceProcessGroup.elements.dimensions) {
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

    protected HashMap<String, ArrayList<Dimension>> createGroupHashTable(Group group, Dimension commonKeywords, Integer alpha)
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

    protected String implode(String separator, String... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length - 1; i++) {
            sb.append(data[i]);
            sb.append(separator);
        }
        sb.append(data[data.length - 1]);
        return sb.toString();
    }

    public UnionResult unionWithArrayList(DimensionSet dimSetA, DimensionSet dimSetB)
    {
        UnionResult r = new UnionResult();

        r.dimSet = new DimensionSet();
        r.alpha = 0;
        r.commonKeywords = new Dimension();
        Integer dimIndex = -1;

        // HashTable for dimSetB keywords construction
        HashMap<Resource, Integer> hashTableB = new HashMap<Resource, Integer>();
        ArrayList<Integer> usedDimsB = new ArrayList<Integer>();
        for (Integer i = 0; i < dimSetB.size(); i++) {
            usedDimsB.add(i);
            /*for (Resource keyword: dimSetB.dimensions.get(i).keywords) {
                hashTableB.put(keyword, i);
            }*/
        }
        for (Dimension dimension: dimSetA.dimensions) {
            Dimension dimResultTemp = new Dimension();
            for (Resource keyword: dimension.keywords) {
                if (hashTableB.containsKey(keyword)) {
                    dimIndex = hashTableB.get(keyword);
                    if (usedDimsB.contains(dimIndex)) {
                        usedDimsB.remove(dimIndex);
                    }
                    dimResultTemp.add(keyword);
                    r.commonKeywords.add(keyword);
                }
            }
            if (dimResultTemp.isEmpty()) {
                r.dimSet.add(dimension);
            }
            else {
                r.dimSet.add(dimResultTemp);
                r.alpha++;
            }
        }
        for (Integer i: usedDimsB) {
            //r.dimSet.add(dimSetB.get(i));
        }

        return r;
    }
}