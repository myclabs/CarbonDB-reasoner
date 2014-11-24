package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mycsense.carbondb.NoUnitException;
import com.mycsense.carbondb.domain.*;

import java.util.HashMap;

public class TypeRepo extends AbstractRepo {

    public TypeRepo(Model model) {
        super(model);
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
                Resource resource = iCat.next();
                ImpactType type;
                try {
                    type = new ImpactType(
                            resource.getURI(),
                            getLabelOrURI(resource),
                            RepoFactory.getUnitsRepo().getUnit(resource)
                    );
                    type.setComponents(getComponentsForImpact(resource));
                    category.addChild(type);
                } catch (NoUnitException e) {
                    log.warn(e.getMessage());
                }
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
                ElementaryFlowType flowType = CarbonOntology.getInstance().getElementaryFlowType(flowTypeResource.getURI());

                components.put(flowType, new Value(value, uncertainty));
            }
        }
        return components;
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
                Resource resource = iCat.next();
                ElementaryFlowType type;
                try {
                    type = new ElementaryFlowType(
                            resource.getURI(),
                            getLabelOrURI(resource),
                            RepoFactory.getUnitsRepo().getUnit(resource)
                    );
                    category.addChild(type);
                } catch (NoUnitException e) {
                    log.warn(e.getMessage());
                }
            }
        }

        return root;
    }
}
