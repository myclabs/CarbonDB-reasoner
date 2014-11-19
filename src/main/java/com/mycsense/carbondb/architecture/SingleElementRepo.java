package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mycsense.carbondb.*;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.Process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SingleElementRepo extends AbstractRepo {

    protected UnitsRepo unitsRepo;
    protected HashMap<String, Resource> processesResourceCache;
    protected HashMap<String, Resource> coefficientsResourceCache;
    protected HashMap<String, Process> processesCache;
    protected HashMap<String, Process> coefficientsCache;
    protected HashMap<String, ElementaryFlowType> flowTypesCache;

    public SingleElementRepo(Model model, UnitsRepo unitsRepo) {
        super(model);
        this.unitsRepo = unitsRepo;
        processesResourceCache = new HashMap<>();
        coefficientsResourceCache = new HashMap<>();
        processesCache = new HashMap<>();
        coefficientsCache = new HashMap<>();
        flowTypesCache = new HashMap<>();
    }

    public Resource getProcessForDimension(Dimension dimension, String unit)
            throws MultipleElementsFoundException, NoElementFoundException
    {
        String elementKey = dimension.toString() + unit;
        if (!processesResourceCache.containsKey(elementKey)) {
            Resource processResource = getElementResourceForDimension(dimension, unit, Datatype.SingleProcess);
            processesResourceCache.put(elementKey, processResource);
        }
        return processesResourceCache.get(elementKey);
    }

    public Resource getCoefficientForDimension(Dimension dimension, String unit)
            throws MultipleElementsFoundException, NoElementFoundException
    {
        String elementKey = dimension.toString() + unit;
        if (!coefficientsResourceCache.containsKey(elementKey)) {
            Resource coefficientResource = getElementResourceForDimension(dimension, unit, Datatype.SingleCoefficient);
            coefficientsResourceCache.put(elementKey, coefficientResource);
        }
        return coefficientsResourceCache.get(elementKey);
    }

    protected Resource getElementResourceForDimension(Dimension dimension, String unit, Resource singleType)
            throws MultipleElementsFoundException, NoElementFoundException
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
            } else if (!getUnitURI(candidate).equals(unit)) {
                i.remove();
            } else {
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
            throw new NoElementFoundException("No " + (singleType == Datatype.SingleProcess ? "process" : "coefficient")
                    + " found with keywords: " + dimension.keywords + " and unit: " + unit);
        }
        if (candidates.size() > 1) {
            throw new MultipleElementsFoundException("Found multiple " + (singleType == Datatype.SingleProcess ? "processes" : "coefficients")
                    + " with keywords: " + dimension.keywords + " and unit " + unit
                    + " (using: " + candidates.get(0).getURI() + ")");
        }
        return candidates.get(0);
    }

    public HashMap<String, ElementaryFlowType> getElementaryFlowTypes() {
        Selector selector = new SimpleSelector(null, RDF.type, (RDFNode) Datatype.ElementaryFlowType);
        StmtIterator iter = model.listStatements( selector );

        HashMap<String, ElementaryFlowType> elementaryFlowTypes = new HashMap<>();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                Resource flowTypeResource = s.getSubject();
                ElementaryFlowType flowType = new ElementaryFlowType(
                    s.getSubject().getURI(),
                    getLabelOrURI(flowTypeResource),
                    RepoFactory.getUnitsRepo().getUnit(flowTypeResource)
                );
                elementaryFlowTypes.put(flowTypeResource.getURI(), flowType);
                flowTypesCache.put(flowTypeResource.getURI(), flowType);
            }
        }
        return elementaryFlowTypes;
    }

    public ArrayList<Resource> getImpactTypes() {
        Selector selector = new SimpleSelector(null, RDF.type, (RDFNode) Datatype.ImpactType);
        StmtIterator iter = model.listStatements( selector );

        ArrayList<Resource> impactTypes = new ArrayList<>();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                impactTypes.add(s.getSubject());
            }
        }
        return impactTypes;
    }

    public Category getImpactTypesTree() {
        Category root = new Category();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.CategoryOfImpactType);
        while (i.hasNext()) {
            Resource categoryResource = i.next();
            Category category = new Category(
                    categoryResource.getURI(),
                    getLabelOrURI(categoryResource),
                    root);

            root.addChild(category);
            ResIterator iCat = model.listResourcesWithProperty(Datatype.belongsToCategoryOfImpactType, categoryResource);
            while (iCat.hasNext()) {
                Resource impactTypeResource = iCat.next();
                HashMap<String, String> impactType = new HashMap<>();
                impactType.put("uri", impactTypeResource.getURI());
                impactType.put("label", getLabelOrURI(impactTypeResource));
                impactType.put("unit", unitsRepo.getUnit(categoryResource).getSymbol());
                category.addChild(impactType);
            }
        }

        return root;
    }

    public Category getElementaryFlowTypesTree() {
        Category root = new Category();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.CategoryOfElementaryFlowType);
        while (i.hasNext()) {
            Resource categoryResource = i.next();
            Category category = new Category(
                    categoryResource.getURI(),
                    getLabelOrURI(categoryResource),
                    root);

            root.addChild(category);
            ResIterator iCat = model.listResourcesWithProperty(Datatype.belongsToCategoryOfElementaryFlowType, categoryResource);
            while (iCat.hasNext()) {
                Resource impactTypeResource = iCat.next();
                HashMap<String, String> elementaryFlowType = new HashMap<>();
                elementaryFlowType.put("uri", impactTypeResource.getURI());
                elementaryFlowType.put("label", getLabelOrURI(impactTypeResource));
                elementaryFlowType.put("unit", unitsRepo.getUnit(categoryResource).getSymbol());
                category.addChild(elementaryFlowType);
            }
        }

        return root;
    }

    public Process getProcess(Resource processResource)
    {
        if (!processesCache.containsKey(processResource.getURI())) {
            Process process = new Process(getElementKeywords(processResource));
            process.setFlows(getElementaryFlowsForProcess(processResource));
            process.setImpacts(getImpactsForProcess(processResource));
            process.setUnit(unitsRepo.getUnit(processResource).getSymbol());
            process.setUnitURI(getUnitURI(processResource));
            processesCache.put(processResource.getURI(), process);
        }
        return processesCache.get(processResource.getURI());
    }

    protected Dimension getElementKeywords(Resource elementResource)
    {
        Selector selector = new SimpleSelector(elementResource, Datatype.hasTag, (RDFNode) null);
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

    public ArrayList<Resource> getSingleProcesses() {
        Selector selector = new SimpleSelector(null, RDF.type, (RDFNode) Datatype.SingleProcess);
        StmtIterator iter = model.listStatements( selector );

        ArrayList<Resource> processes = new ArrayList<>();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                processes.add(s.getSubject());
            }
        }
        return processes;
    }

    public HashMap<Resource, Value> getEmissionsForProcess(Resource process)
    {
        HashMap<Resource, Value> emissions = new HashMap<>();
        StmtIterator iter = process.listProperties(Datatype.hasFlow);
        Double conversionFactor = 1.0;
        if (iter.hasNext()) {
            String unitID = unitsRepo.getUnit(process).getRef();
            if (!unitID.equals("")) {
                conversionFactor = unitsRepo.getConversionFactor(unitID);
            }
        }

        while (iter.hasNext()) {
            Resource emission = iter.nextStatement().getResource();
            if (emission.hasProperty(Datatype.hasElementaryFlowType) && null != emission.getProperty(Datatype.hasElementaryFlowType)
                    && emission.hasProperty(Datatype.value) && null != emission.getProperty(Datatype.value)) {
                Resource nature = emission.getProperty(Datatype.hasElementaryFlowType).getResource();
                Double value = emission.getProperty(Datatype.value).getDouble() / conversionFactor;
                Double uncertainty = getUncertainty(emission);

                emissions.put(nature, new Value(value, uncertainty));
            }
        }
        return emissions;
    }

    public HashMap<String, Value> getCalculatedEmissionsForProcess(Resource process)
    {
        HashMap<String, Value> emissions = new HashMap<>();
        StmtIterator iter = process.listProperties(Datatype.hasCalculatedFlow);

        while (iter.hasNext()) {
            Resource emission = iter.nextStatement().getResource();
            Resource nature = emission.getProperty(Datatype.hasElementaryFlowType).getResource();
            Double value = emission.getProperty(Datatype.value).getDouble();
            Double uncertainty = getUncertainty(emission);
            emissions.put(nature.getURI(), new Value(value, uncertainty));
        }
        return emissions;
    }

    public ArrayList<ElementaryFlow> getElementaryFlowsForProcess(Resource process)
    {
        ArrayList<ElementaryFlow> flows = new ArrayList<>();
        StmtIterator iter = process.listProperties(Datatype.hasFlow);
        Double conversionFactor = 1.0;
        if (iter.hasNext()) {
            String unitID = unitsRepo.getUnit(process).getRef();
            if (!unitID.equals("")) {
                conversionFactor = unitsRepo.getConversionFactor(unitID);
            }
        }

        while (iter.hasNext()) {
            Resource emission = iter.nextStatement().getResource();
            if (emission.hasProperty(Datatype.hasElementaryFlowType) && null != emission.getProperty(Datatype.hasElementaryFlowType)
                    && emission.hasProperty(Datatype.value) && null != emission.getProperty(Datatype.value)) {
                ElementaryFlowType flowType = flowTypesCache.get(emission.getProperty(Datatype.hasElementaryFlowType).getResource().getURI());
                Value value = new Value(emission.getProperty(Datatype.value).getDouble() / conversionFactor, getUncertainty(emission));

                flows.add(new ElementaryFlow(flowType, value));
            }
        }
        return flows;
    }

    public HashMap<String, Value> getImpactsForProcess(Resource process)
    {
        HashMap<String, Value> impacts = new HashMap<>();
        StmtIterator iter = process.listProperties(Datatype.hasImpact);

        while (iter.hasNext()) {
            Resource emission = iter.nextStatement().getResource();
            if (emission.hasProperty(Datatype.hasImpactType) && null != emission.getProperty(Datatype.hasImpactType)
                    && emission.hasProperty(Datatype.value) && null != emission.getProperty(Datatype.value)) {
                Resource impactType = emission.getProperty(Datatype.hasImpactType).getResource();
                Double value = emission.getProperty(Datatype.value).getDouble();
                Double uncertainty = getUncertainty(emission);

                impacts.put(impactType.getURI(), new Value(value, uncertainty));
            }
        }
        return impacts;
    }

    public HashMap<Resource, Value> getComponentsForImpact(Resource impactType)
    {
        HashMap<Resource, Value> components = new HashMap<>();
        StmtIterator iter = impactType.listProperties(Datatype.hasComponentOfImpactType);

        while (iter.hasNext()) {
            Resource component = iter.nextStatement().getResource();
            if (component.hasProperty(Datatype.isBasedOnElementaryFlowType)
                && null != component.getProperty(Datatype.isBasedOnElementaryFlowType)
                && component.hasProperty(Datatype.value) && null != component.getProperty(Datatype.value)
            ) {
                Resource elementaryFlowType = component.getProperty(Datatype.isBasedOnElementaryFlowType).getResource();
                Double value = component.getProperty(Datatype.value).getDouble();
                Double uncertainty = getUncertainty(component);

                components.put(elementaryFlowType, new Value(value, uncertainty));
            }
        }
        return components;
    }

    public Double getUncertainty(Resource resource)
    {
        if (resource.hasProperty(Datatype.uncertainty) && null != resource.getProperty(Datatype.uncertainty)) {
            return resource.getProperty(Datatype.uncertainty).getDouble();
        }
        return 0.0;
    }

    public Resource createProcess(Dimension dimension, String unitURI)
    {
        Process process = new Process(dimension);
        process.setUnitURI(unitURI);
        Resource processResource = model.createResource(Datatype.getURI() + "sp/" + AnonId.create().toString())
                .addProperty(RDF.type, Datatype.SingleProcess);
        if (null != unitURI) {
            processResource.addProperty(Datatype.hasUnit, model.createResource(unitURI));
        }
        for (Keyword keyword: dimension.keywords) {
            Resource keywordResource = model.createResource(keyword.name);
            processResource.addProperty(Datatype.hasTag, keywordResource);
        }
        return processResource;
    }

    public void addCumulatedEcologicalFlow(Resource process, Resource elementaryFlowNature, double value, double uncertainty)
    {
        process.addProperty(Datatype.hasCalculatedFlow,
                model.createResource(Datatype.getURI() + AnonId.create().toString())
                        .addProperty(Datatype.hasElementaryFlowType, elementaryFlowNature)
                        .addProperty(Datatype.value, model.createTypedLiteral(value))
                        .addProperty(Datatype.uncertainty, model.createTypedLiteral(uncertainty))
                        .addProperty(RDF.type, Datatype.CalculateElementaryFlow));
    }

    public void addImpact(Resource process, Resource impactType, double value, double uncertainty)
    {
        process.addProperty(Datatype.hasImpact,
                model.createResource(Datatype.getURI() + AnonId.create().toString())
                        .addProperty(Datatype.hasImpactType, impactType)
                        .addProperty(Datatype.value, model.createTypedLiteral(value))
                        .addProperty(Datatype.uncertainty, model.createTypedLiteral(uncertainty))
                        .addProperty(RDF.type, Datatype.Impact));
    }

    public Dimension getSingleElementKeywords(Resource singleElement)
    {
        Selector selector = new SimpleSelector(singleElement, Datatype.hasTag, (RDFNode) null);
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

}
