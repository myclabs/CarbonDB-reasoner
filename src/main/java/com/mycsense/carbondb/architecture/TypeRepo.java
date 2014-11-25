package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mycsense.carbondb.NoUnitException;
import com.mycsense.carbondb.NotFoundException;
import com.mycsense.carbondb.domain.*;

import java.util.HashMap;

public class TypeRepo extends AbstractRepo {

    public TypeRepo(Model model) {
        super(model);
    }

    public TypeCategory getImpactTypesTree() {
        Unit defaultUnit = new Unit("u/one", "un");
        TypeCategory root = new TypeCategory(defaultUnit);

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.CategoryOfImpactType);
        while (i.hasNext()) {
            Resource categoryResource = i.next();
            Unit categoryUnit;
            try {
                categoryUnit = RepoFactory.getUnitsRepo().getUnit(categoryResource);
            } catch (NoUnitException e) {
                log.warn(e.getMessage() + " - using " + defaultUnit.getId() + " instead");
                // @todo add default unit to the config
                categoryUnit = defaultUnit;
            }
            TypeCategory category = new TypeCategory(
                    getId(categoryResource),
                    getLabelOrURI(categoryResource),
                    root,
                    categoryUnit);

            root.addChild(category);
            ResIterator iCat = model.listResourcesWithProperty(Datatype.belongsToCategoryOfImpactType, categoryResource);
            while (iCat.hasNext()) {
                Resource resource = iCat.next();
                ImpactType type;
                type = new ImpactType(
                        getId(resource),
                        getLabelOrURI(resource),
                        categoryUnit
                );
                type.setComponents(getComponentsForImpact(resource));
                category.addChild(type);
            }
        }

        return root;
    }

    protected HashMap<ElementaryFlowType, Value> getComponentsForImpact(Resource impactType)
    {
        HashMap<ElementaryFlowType, Value> components = new HashMap<>();
        StmtIterator iter = impactType.listProperties(Datatype.hasComponentOfImpactType);

        while (iter.hasNext()) {
            Resource component = iter.nextStatement().getResource();
            if (component.hasProperty(Datatype.isBasedOnElementaryFlowType)
                    && null != component.getProperty(Datatype.isBasedOnElementaryFlowType)
                    && component.hasProperty(Datatype.value) && null != component.getProperty(Datatype.value)
                    ) {
                Resource flowTypeResource = component.getProperty(Datatype.isBasedOnElementaryFlowType).getResource();
                Double value = component.getProperty(Datatype.value).getDouble();
                Double uncertainty = getUncertainty(component);
                try {
                    ElementaryFlowType flowType = CarbonOntology.getInstance().getElementaryFlowType(getId(flowTypeResource));
                    components.put(flowType, new Value(value, uncertainty));
                } catch (NotFoundException e) {
                    log.warn(e.getMessage());
                }
            }
        }
        return components;
    }

    public TypeCategory getElementaryFlowTypesTree() {
        Unit defaultUnit = new Unit("u/one", "un");
        TypeCategory root = new TypeCategory(defaultUnit);

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.CategoryOfElementaryFlowType);
        while (i.hasNext()) {
            Resource categoryResource = i.next();
            Unit categoryUnit;
            try {
                categoryUnit = RepoFactory.getUnitsRepo().getUnit(categoryResource);
            } catch (NoUnitException e) {
                log.warn(e.getMessage() + " - using " + defaultUnit.getId() + " instead");
                // @todo add default unit to the config
                categoryUnit = defaultUnit;
            }
            TypeCategory category = new TypeCategory(
                    getId(categoryResource),
                    getLabelOrURI(categoryResource),
                    root,
                    categoryUnit);

            root.addChild(category);
            ResIterator iCat = model.listResourcesWithProperty(Datatype.belongsToCategoryOfElementaryFlowType, categoryResource);
            while (iCat.hasNext()) {
                Resource resource = iCat.next();
                ElementaryFlowType type;
                type = new ElementaryFlowType(
                        getId(resource),
                        getLabelOrURI(resource),
                        categoryUnit
                );
                category.addChild(type);
            }
        }

        return root;
    }
}
