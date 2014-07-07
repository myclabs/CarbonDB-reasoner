package com.mycsense.carbondb; 

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.util.FileManager;

public class Reader {

    public Model model;

    public Reader (Model model) {
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

    public Resource getProcessForDimension(Dimension dimension)
    {
        return getElementForDimension(dimension, Datatype.SingleProcess);
    }

    public Resource getCoefficientForDimension(Dimension dimension)
    {
        return getElementForDimension(dimension, Datatype.SingleCoefficient);
    }

    protected Resource getElementForDimension(Dimension dimension, Resource singleType)
    {
        // @todo: throw an exception instead of returning null when the element could not be found?
        if (dimension.size() == 0) {
            return (Resource) null;
        }
        Iterator<Keyword> iter = dimension.keywords.iterator();
        Keyword keyword = iter.next();
        Resource keywordResource = ResourceFactory.createResource(keyword.getName());
        Selector selector = new SimpleSelector(null, Datatype.hasKeyword, keywordResource);
        ResIterator statementIter = model.listSubjectsWithProperty(Datatype.hasKeyword, keywordResource);
        List<Resource> candidates = statementIter.toList();
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
                    i.remove();
                }
            }
        }

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

        if (candidates.isEmpty()) {
            return (Resource) null;
        }
        return candidates.get(0);
    }

    public Group getGroup(String groupId)
    {
        return getGroup(model.getResource(Datatype.getURI() + groupId));
    }

    public Group getGroup(Resource groupResource)
    {
        Group group = new Group(getGroupDimSet(groupResource));
        group.setLabel(getLabelOrURI(groupResource));
        group.setURI(groupResource.getURI());
        group.setId(groupResource.getURI().replace(Datatype.getURI(), ""));
        return group;
    }

    public ArrayList<Group> getProcessGroups()
    {
        return getGroups(Datatype.Processfamily);
    }

    public ArrayList<Group> getCoefficientGroups()
    {
        return getGroups(Datatype.CoefficientFamily);
    }

    protected ArrayList<Group> getGroups(Resource groupType)
    {
        ArrayList<Group> groups = new ArrayList<Group>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, groupType);
        while (i.hasNext()) {
            Resource groupResource = i.next();
            groups.add(getGroup(groupResource));
        }

        return groups;
    }

    protected String getLabelOrURI(Resource resource)
    {
        Statement label = resource.getProperty(RDFS.label);
        if (null == label) {
            return resource.getURI();
        }
        else {
            return label.getString();
        }
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
                Keyword keyword = new Keyword(s.getObject().toString());
                keyword.setLabel(getLabelOrURI(s.getObject().asResource()));
                dim.add(keyword);
            }
        }
        return dim;
    }

    public ArrayList<Resource> getElementaryFlowNatures() {
        Selector selector = new SimpleSelector(null, RDF.type, (RDFNode) Datatype.ElementaryFlowNature);
        StmtIterator iter = model.listStatements( selector );

        ArrayList<Resource> elementaryFlowNatures = new ArrayList<Resource>();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                elementaryFlowNatures.add(s.getSubject());
            }
        }
        return elementaryFlowNatures;
    }

    public ArrayList<Resource> getSingleProcesses() {
        Selector selector = new SimpleSelector(null, RDF.type, (RDFNode) Datatype.SingleProcess);
        StmtIterator iter = model.listStatements( selector );

        ArrayList<Resource> processes = new ArrayList<Resource>();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                processes.add(s.getSubject());
            }
        }
        return processes;
    }

    public ArrayList<Resource> getRelationsForProcess(Resource process) {
        Selector selector = new SimpleSelector(null, Datatype.hasOrigin, process);
        StmtIterator iter = model.listStatements( selector );

        ArrayList<Resource> relations = new ArrayList<Resource>();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                relations.add(s.getSubject());
            }
        }
        return relations;
    }

    public Double getCoefficientValueForRelation(Resource relation) {
        Resource coefficient = relation.getProperty(Datatype.hasWeight).getResource();

        return coefficient.getProperty(Datatype.value).getDouble();
    }

    public HashMap<Resource, Double> getEmissionsForProcess(Resource process)
    {
        HashMap<Resource, Double> emissions = new HashMap<Resource, Double>();
        StmtIterator iter = process.listProperties(Datatype.emits);

        while (iter.hasNext()) {
            Resource emission = iter.nextStatement().getResource();
            Resource nature = emission.getProperty(Datatype.hasNature).getResource();
            double emissionValue = emission.getProperty(Datatype.value).getDouble();

            emissions.put(nature, emissionValue);
        }
        return emissions;
    }

    public HashMap<Resource, Double> getCalculatedEmissionsForProcess(Resource process)
    {
        HashMap<Resource, Double> emissions = new HashMap<Resource, Double>();
        StmtIterator iter = process.listProperties(Datatype.emits);

        while (iter.hasNext()) {
            Resource emission = iter.nextStatement().getResource();
            if (model.contains(emission, RDF.type, (RDFNode) Datatype.CalculateElementaryFlow)) {
                Resource nature = emission.getProperty(Datatype.hasNature).getResource();
                double emissionValue = emission.getProperty(Datatype.value).getDouble();
                emissions.put(nature, emissionValue);
            }
        }
        return emissions;
    }
}