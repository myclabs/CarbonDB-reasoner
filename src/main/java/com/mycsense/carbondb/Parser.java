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

public class Parser {

    //protected Model model;
    public Model model;
    protected InfModel infModel;
    protected ArrayList<Resource> processes;
    protected ArrayList<Resource> elementaryFlowNatures;

    Parser (Model model) {
        this.model = model;
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

    public void tests (String inputFileName) {

        model = ModelFactory.createDefaultModel();

        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException(
                        "File: " + inputFileName + " not found");
        }

        model.read(in, null);

        // Those lines shall be converted to an integration test (+ translate)
        Dimension dimA1 = new Dimension();
        dimA1.add(new Keyword("http://www.myc-sense.com/ontologies/bc#kw1"));
        dimA1.add(new Keyword("http://www.myc-sense.com/ontologies/bc#kw2"));
        System.out.println("+++ RDF file readed +++");
        System.out.println(getGroupDimSet(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")));
        System.out.println("+++ group fp1 +++");
        System.out.println(getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fp1")));
        System.out.println("+++ group fc +++");
        System.out.println(getGroup(ResourceFactory.createResource("http://www.myc-sense.com/ontologies/bc#fc")));
        System.out.println(getProcessForDimension(dimA1));

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
        return getElementForDimension(dimension, Datatype.SingleProcess);
    }

    protected Resource getCoefficientForDimension(Dimension dimension)
    {
        return getElementForDimension(dimension, Datatype.SingleCoefficient);
    }

    protected Resource getElementForDimension(Dimension dimension, Resource singleType)
    {
        // @todo: throw an exception instead of returning null when the element could not be found?
        System.out.println("--- Process exists called ---");
        if (dimension.size() == 0) {
            return (Resource) null;
        }
        Iterator<Keyword> iter = dimension.keywords.iterator();
        Keyword keyword = iter.next();
        Resource keywordResource = ResourceFactory.createResource(keyword.getName());
        Selector selector = new SimpleSelector(null, Datatype.hasKeyword, keywordResource);
        ResIterator statementIter = model.listSubjectsWithProperty(Datatype.hasKeyword, keywordResource);
        //StmtIterator statementIter = model.listStatements( selector );
        List<Resource> candidates = statementIter.toList();
        System.out.println(candidates);
        Iterator<Resource> i = candidates.iterator();
        while (i.hasNext()) {
            Resource candidate = i.next();
            if (!model.contains(candidate, RDF.type, (RDFNode) singleType)) {
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
            keywordResource = ResourceFactory.createResource(keyword.getName());
            i = candidates.iterator();
            while (i.hasNext()) {
                Resource candidate = i.next();
                if (!model.contains(candidate, Datatype.hasKeyword, (RDFNode) keywordResource)) {
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

    public Group getGroup(String groupName)
    {
        return getGroup(ResourceFactory.createResource(Datatype.getURI() + groupName));
    }

    public Group getGroup(Resource groupResource)
    {
        Group group = new Group(getGroupDimSet(groupResource));
        return group;
    }

    protected DimensionSet getGroupDimSet(Resource groupResource)
    {
        Selector selector = new SimpleSelector(groupResource, Datatype.hasDimension, (RDFNode) null);
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

    protected Dimension getDimensionKeywords(Resource dimensionResource)
    {
        Selector selector = new SimpleSelector(dimensionResource, Datatype.hasKeyword, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        Dimension dim = new Dimension();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                dim.add(new Keyword(s.getObject().toString()));
            }
        }
        return dim;
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
}