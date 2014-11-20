package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mycsense.carbondb.*;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.Process;

import javax.xml.crypto.Data;
import java.util.*;

public class SingleElementRepo extends AbstractRepo {

    protected UnitsRepo unitsRepo;
    protected HashMap<String, Resource> processesResourceCache;
    protected HashMap<String, Resource> coefficientsResourceCache;
    protected HashMap<String, Process> processesCache;
    protected HashMap<String, Coefficient> coefficientsCache;
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

    public Process getProcess(Resource processResource)
    {
        if (!processesCache.containsKey(processResource.getURI())) {
            Unit unit = RepoFactory.unitsRepo.getUnit(processResource);
            Process process = new Process(getElementKeywords(processResource), unit);
            getElementaryFlowsForProcess(process, processResource);
            getImpactsForProcess(process, processResource);
            processesCache.put(processResource.getURI(), process);
        }
        return processesCache.get(processResource.getURI());
    }

    public Coefficient getCoefficient(Resource coefficientResource)
    {
        if (!coefficientsCache.containsKey(coefficientResource.getURI())) {
            Unit unit = RepoFactory.unitsRepo.getUnit(coefficientResource);
            Double conversionFactor = unit.getConversionFactor();
            Value value = new Value(coefficientResource.getProperty(Datatype.value).getDouble() / conversionFactor,
                    getUncertainty(coefficientResource));
            Coefficient coefficient = new Coefficient(getElementKeywords(coefficientResource), unit, value);
            coefficientsCache.put(coefficientResource.getURI(), coefficient);
        }
        return coefficientsCache.get(coefficientResource.getURI());
    }

    public ArrayList<Process> getProcesses() {
        ArrayList<Process> processes = new ArrayList<>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.SingleProcess);
        while (i.hasNext()) {
            Resource resource = i.next();
            processes.add(getProcess(resource));
        }

        return processes;
    }

    public ArrayList<Coefficient> getCoefficients() {
        ArrayList<Coefficient> coefficients = new ArrayList<>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.SingleCoefficient);
        while (i.hasNext()) {
            Resource resource = i.next();
            coefficients.add(getCoefficient(resource));
        }

        return coefficients;
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

    protected HashMap<String, ElementaryFlow> getElementaryFlowsForProcess(Process process, Resource resource)
    {
        Double conversionFactor = process.getUnit().getConversionFactor();
        HashMap<String, ElementaryFlow> flows = new HashMap<>();
        StmtIterator iter = resource.listProperties(Datatype.hasFlow);
        while (iter.hasNext()) {
            Resource emission = iter.nextStatement().getResource();
            if (emission.hasProperty(Datatype.hasElementaryFlowType) && null != emission.getProperty(Datatype.hasElementaryFlowType)
                    && emission.hasProperty(Datatype.value) && null != emission.getProperty(Datatype.value)) {
                String typeURI = emission.getProperty(Datatype.hasElementaryFlowType).getResource().getURI();
                ElementaryFlowType flowType = CarbonOntology.getInstance().getElementaryFlowType(typeURI);
                Value value = new Value(emission.getProperty(Datatype.value).getDouble() / conversionFactor, getUncertainty(emission));

                try {
                    process.addFlow(new ElementaryFlow(flowType, value));
                } catch (AlreadyExistsException e) {
                    log.warn(e.getMessage());
                }
            }
        }
        return flows;
    }

    protected HashMap<String, Impact> getImpactsForProcess(Process process, Resource resource)
    {
        HashMap<String, Impact> impacts = new HashMap<>();
        StmtIterator iter = resource.listProperties(Datatype.hasImpact);

        while (iter.hasNext()) {
            Resource emission = iter.nextStatement().getResource();
            if (emission.hasProperty(Datatype.hasImpactType) && null != emission.getProperty(Datatype.hasImpactType)
                    && emission.hasProperty(Datatype.value) && null != emission.getProperty(Datatype.value)) {
                String typeURI = emission.getProperty(Datatype.hasImpactType).getResource().getURI();
                ImpactType impactType = CarbonOntology.getInstance().getImpactType(typeURI);
                Value value = new Value(emission.getProperty(Datatype.value).getDouble(), getUncertainty(emission));

                try {
                    process.addImpact(new Impact(impactType, value));
                } catch (AlreadyExistsException e) {
                    log.warn(e.getMessage());
                }
            }
        }
        return impacts;
    }

    public HashMap<Resource, Value> getComponentsForImpact(Resource impactType)
    {
        // @todo moved this logic to the ImpactType class and the extraction to the TypeRepo
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
