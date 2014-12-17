/*
 * Copyright 2014, by Benjamin Bertin and Contributors.
 *
 * This file is part of CarbonDB-reasoner project <http://www.carbondb.org>
 *
 * CarbonDB-reasoner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * CarbonDB-reasoner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CarbonDB-reasoner.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributor(s): -
 *
 */

package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import com.mycsense.carbondb.NoUnitException;
import com.mycsense.carbondb.NotFoundException;
import com.mycsense.carbondb.UnrecogniedUnitException;
import com.mycsense.carbondb.domain.CarbonOntology;
import com.mycsense.carbondb.domain.ElementaryFlowType;
import com.mycsense.carbondb.domain.ImpactType;
import com.mycsense.carbondb.domain.TypeCategory;
import com.mycsense.carbondb.domain.Unit;
import com.mycsense.carbondb.domain.Value;

import java.util.HashMap;

public class TypeRepo extends AbstractRepo {

    public TypeRepo(Model model) {
        super(model);
    }

    public TypeCategory getImpactTypesTree() {
        Unit defaultUnit = new Unit("u/one", "one", "un");
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
            } catch (UnrecogniedUnitException e) {
                log.warn(e.getMessage() + " for the category " + categoryResource.getURI()
                        + " - using " + defaultUnit.getId() + " instead");
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
                type.setCategory(category);
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
        Unit defaultUnit = new Unit("u/one", "one", "un");
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
            } catch (UnrecogniedUnitException e) {
                log.warn(e.getMessage() + " for the category " + categoryResource.getURI()
                         + " - using " + defaultUnit.getId() + " instead");
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
                type.setCategory(category);
            }
        }

        return root;
    }
}
