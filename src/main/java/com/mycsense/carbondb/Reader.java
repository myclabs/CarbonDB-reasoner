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

import com.mycsense.carbondb.dimension.Orientation;
import com.mycsense.carbondb.group.Type;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

public class Reader {

    public Model model;
    protected HashMap<String, Double> unitsConversionFactors = new HashMap<String, Double>();

    public Reader (Model model) {
        this.model = model;
    }

    public Category getCategoriesTree() {
        Category root = new Category();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.Category);
        while (i.hasNext()) {
            Resource categoryResource = i.next();
            if (!categoryResource.hasProperty(Datatype.hasParent)
                || categoryResource.getProperty(Datatype.hasParent) == null
            ) {
                root.addChild(getCategory(categoryResource, root));
            }
        }

        return root;
    }

    protected Category getCategory(Resource categoryResource, Category parentCategory) {
        Category category = new Category(
            categoryResource.getURI(),
            getLabelOrURI(categoryResource),
            parentCategory);
        ResIterator i = model.listResourcesWithProperty(Datatype.hasParent, categoryResource);
        while (i.hasNext()) {
            Resource subCategoryResource = i.next();
            category.addChild(getCategory(subCategoryResource, category));
        }
        i = model.listResourcesWithProperty(Datatype.hasCategory, categoryResource);
        while (i.hasNext()) {
            Resource groupResource = i.next();
            category.addChild(getSimpleGroup(groupResource));
        }

        return category;
    }

    public ArrayList<MacroRelation> getMacroRelations() {
        ArrayList<MacroRelation> macroRelations = new ArrayList<MacroRelation>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.Relation);
        while (i.hasNext()) {
            Resource macroRelResource = i.next();
            MacroRelation macroRelation = new MacroRelation(
                getGroup(macroRelResource.getProperty(Datatype.hasOrigin).getResource()),
                getGroup(macroRelResource.getProperty(Datatype.hasWeight).getResource()),
                getGroup(macroRelResource.getProperty(Datatype.hasDestination).getResource())
            );
            macroRelation.setURI(macroRelResource.getURI());
            if (macroRelResource.hasProperty(Datatype.exponent)
                && null != macroRelResource.getProperty(Datatype.exponent)
            ) {
                macroRelation.setExponent(macroRelResource.getProperty(Datatype.exponent).getInt());
            }
            macroRelations.add(macroRelation);
        }

        return macroRelations;
    }

    public ArrayList<MacroRelation> getMacroRelationsForProcessGroup(Resource group) {
        ArrayList<MacroRelation> macroRelations = new ArrayList<MacroRelation>();

        ResIterator i = model.listSubjectsWithProperty(Datatype.involves, group);
        while (i.hasNext()) {
            Resource macroRelation = i.next();
            macroRelations.add(new MacroRelation(
                getSimpleGroup(macroRelation.getProperty(Datatype.hasOrigin).getResource()),
                getSimpleGroup(macroRelation.getProperty(Datatype.hasWeight).getResource()),
                getSimpleGroup(macroRelation.getProperty(Datatype.hasDestination).getResource())
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
        System.out.println(getProcessForDimension(dimA1, null));

        /*try {
            FileOutputStream out = new FileOutputStream("ontologies/bc-test-inferred-java.rdf");
            model.write(out);
        }
        catch (IOException e) {
            System.out.println("error while writing the onto file");
        }*/
    }

    public Resource getProcessForDimension(Dimension dimension, String unit)
    {
        return getElementForDimension(dimension, unit, Datatype.SingleProcess);
    }

    public Resource getCoefficientForDimension(Dimension dimension, String unit)
    {
        return getElementForDimension(dimension, unit, Datatype.SingleCoefficient);
    }

    protected Resource getElementForDimension(Dimension dimension, String unit, Resource singleType)
    {
        // @todo: throw an exception instead of returning null when the element could not be found?
        if (dimension.size() == 0) {
            return (Resource) null;
        }
        // We consider the candidates linked to one of the dimension's keywords...
        Iterator<Keyword> iter = dimension.keywords.iterator();
        Keyword keyword = iter.next();
        Resource keywordResource = ResourceFactory.createResource(keyword.getName());
        ResIterator statementIter = model.listSubjectsWithProperty(Datatype.hasTag, keywordResource);
        List<Resource> candidates = statementIter.toList();
        Iterator<Resource> i = candidates.iterator();
        // ...and remove the candidates with a wrong type or not enough keywords or not the good unit
        while (i.hasNext()) {
            Resource candidate = i.next();
            if (!model.contains(candidate, RDF.type, (RDFNode) singleType)) {
                i.remove();
            }
            else if (getUnitURI(candidate) != unit) {
                i.remove();
            }
            else {
                NodeIterator nodeIter = model.listObjectsOfProperty(candidate, Datatype.hasTag);
                int listSize = nodeIter.toList().size();
                if (listSize != dimension.size()) {
                    i.remove();
                }
            }
        }

        // Then we iterate over all the remaining keywords in the dimension...
        while (iter.hasNext()) {
            keyword = iter.next();
            keywordResource = ResourceFactory.createResource(keyword.getName());
            i = candidates.iterator();
            // ...and remove the candidates that doesn't have a link to a keyword in the dimension
            while (i.hasNext()) {
                Resource candidate = i.next();
                if (!model.contains(candidate, Datatype.hasTag, (RDFNode) keywordResource)) {
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
        Group group = new Group(getGroupDimSet(groupResource), getGroupCommonKeywords(groupResource));
        group.setLabel(getLabelOrURI(groupResource));
        group.setURI(groupResource.getURI());
        group.setId(groupResource.getURI().replace(Datatype.getURI(), ""));
        group.setUnit(getUnit(groupResource));
        group.setUnitURI(getUnitURI(groupResource));
        group.setType(groupResource.hasProperty(RDF.type, Datatype.ProcessGroup) ? Type.PROCESS : Type.COEFFICIENT);
        return group;
    }

    public Group getSimpleGroup(Resource groupResource)
    {
        Group group = new Group();
        group.setLabel(getLabelOrURI(groupResource));
        group.setURI(groupResource.getURI());
        group.setId(groupResource.getURI().replace(Datatype.getURI(), ""));
        group.setUnit(getUnit(groupResource));
        group.setUnitURI(getUnitURI(groupResource));
        group.setType(groupResource.hasProperty(RDF.type, Datatype.ProcessGroup) ? Type.PROCESS : Type.COEFFICIENT);
        return group;
    }

    protected String getUnit(Resource element)
    {
        if (element.hasProperty(Datatype.hasUnit) && null != element.getProperty(Datatype.hasUnit)) {
            Resource unit = element.getProperty(Datatype.hasUnit).getResource();
            if (unit.hasProperty(Datatype.foreignUnitID) && null != unit.getProperty(Datatype.foreignUnitID)) {
                return unit.getProperty(Datatype.foreignUnitID).getString();
            }
        }
        return new String();
    }

    protected String getUnitURI(Resource element)
    {
        if (element.hasProperty(Datatype.hasUnit) && null != element.getProperty(Datatype.hasUnit)) {
            return element.getProperty(Datatype.hasUnit).getResource().getURI();
        }
        return null;
    }

    public ArrayList<Group> getProcessGroups()
    {
        return getGroups(Datatype.ProcessGroup);
    }

    public ArrayList<Group> getCoefficientGroups()
    {
        return getGroups(Datatype.CoefficientGroup);
    }

    public ArrayList<Group> getGroups() {
        return getGroups(Datatype.Group);
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
        StmtIterator iter = resource.listProperties(RDFS.label);
        String label = resource.getURI();
        if (iter.hasNext()) {
            boolean enFound = false;
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                if (s.getLanguage().equals("en")) {
                    label = s.getString();
                    enFound = true;
                }
            }
            if (!enFound) {
                Statement s = resource.getProperty(RDFS.label);
                label = s.getString();
            }
        }
        return label;
    }

    protected DimensionSet getGroupDimSet(Resource groupResource)
    {
        Selector selector = new SimpleSelector(groupResource, Datatype.hasDimension, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        DimensionSet dimSet = new DimensionSet();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                Resource dimensionResource = s.getObject().asResource();
                Dimension dim = getDimensionKeywords(dimensionResource);
                if (groupResource.hasProperty(Datatype.hasHorizontalDimension, dimensionResource))
                    dim.setOrientation(Orientation.HORIZONTAL);
                else if (groupResource.hasProperty(Datatype.hasVerticalDimension, dimensionResource))
                    dim.setOrientation(Orientation.VERTICAL);
                dimSet.add(dim);
            }
        }
        return dimSet;
    }

    protected Dimension getGroupCommonKeywords(Resource groupResource)
    {
        Selector selector = new SimpleSelector(groupResource, Datatype.hasCommonKeyword, (RDFNode) null);
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

    public Keyword getKeyword(Resource keywordResource) {
        Keyword keyword = new Keyword(keywordResource.getURI());
        keyword.setLabel(getLabelOrURI(keywordResource));
        return keyword;
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
        Double value = coefficient.getProperty(Datatype.value).getDouble();
        int exponent = relation.getProperty(Datatype.exponent).getInt();
        String unitID = getUnit(coefficient);
        value *= getUnitConversionFactor(unitID);
        if (-1 == exponent) {
            value = 1 / value;
        }
        return value;
    }

    public Double getCoefficientUncertaintyForRelation(Resource relation) {
        Resource coefficient = relation.getProperty(Datatype.hasWeight).getResource();
        return getUncertainty(coefficient);
    }

    public HashMap<Resource, Value> getEmissionsForProcess(Resource process)
    {
        HashMap<Resource, Value> emissions = new HashMap<Resource, Value>();
        StmtIterator iter = process.listProperties(Datatype.hasFlow);
        Double conversionFactor = new Double(1.0);
        if (iter.hasNext()) {
            String unitID = getUnit(process);
            if (!unitID.equals("")) {
                conversionFactor = getUnitConversionFactor(unitID);
            }
        }

        while (iter.hasNext()) {
            Resource emission = iter.nextStatement().getResource();
            if (emission.hasProperty(Datatype.hasNature) && null != emission.getProperty(Datatype.hasNature)
                && emission.hasProperty(Datatype.value) && null != emission.getProperty(Datatype.value)) {
                Resource nature = emission.getProperty(Datatype.hasNature).getResource();
                Double value = emission.getProperty(Datatype.value).getDouble() / conversionFactor;
                Double uncertainty = getUncertainty(emission);

                emissions.put(nature, new Value(value, uncertainty));
            }
        }
        return emissions;
    }

    public Double getUncertainty(Resource resource)
    {
        if (resource.hasProperty(Datatype.uncertainty) && null != resource.getProperty(Datatype.uncertainty)) {
            return resource.getProperty(Datatype.uncertainty).getDouble();
        }
        return new Double(0);
    }

    public HashMap<Resource, Value> getCalculatedEmissionsForProcess(Resource process)
    {
        HashMap<Resource, Value> emissions = new HashMap<Resource, Value>();
        StmtIterator iter = process.listProperties(Datatype.hasCalculatedFlow);

        while (iter.hasNext()) {
            Resource emission = iter.nextStatement().getResource();
            Resource nature = emission.getProperty(Datatype.hasNature).getResource();
            Double value = emission.getProperty(Datatype.value).getDouble();
            Double uncertainty = getUncertainty(emission);
            emissions.put(nature, new Value(value, uncertainty));
        }
        return emissions;
    }

    public Double getUnitConversionFactor(String unitID)
    {
        if (!unitsConversionFactors.containsKey(unitID)) {
            String unit;
            Response response = ClientBuilder.newClient()
                    .target("http://units.myc-sense.com/api")
                    .path("unit-of-reference")
                    .path(unitID)
                    .request(MediaType.TEXT_PLAIN_TYPE)
                    .get();
            if (response.getStatus() == 200) {
                JSONObject obj = new JSONObject(response.readEntity(String.class));
                String unitOfReference = obj.getString("id");
                if (unitID.equals(unitOfReference)) {
                    unitsConversionFactors.put(unitID, new Double(1.0));
                }
                else {
                    response = ClientBuilder.newClient()
                            .target("http://units.myc-sense.com/api")
                            .path("conversion-factor")
                            .queryParam("unit2", unitID)
                            .queryParam("unit1", unitOfReference)
                            .request(MediaType.TEXT_PLAIN_TYPE)
                            .get();
                    if (response.getStatus() == 200) {
                        Double conversionFactor = new Double(response.readEntity(String.class));
                        unitsConversionFactors.put(unitID, conversionFactor);
                    }
                    else {
                        // it must be incompatible units
                        unitsConversionFactors.put(unitID, new Double(1.0));
                    }
                }
            }
            else {
                // unit not found (or error)
                System.out.println("unit not found: " + unitID);
                unitsConversionFactors.put(unitID, new Double(1.0));
            }
        }
        return unitsConversionFactors.get(unitID);
    }
}