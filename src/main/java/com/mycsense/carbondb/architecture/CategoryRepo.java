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
import com.hp.hpl.jena.vocabulary.RDF;
import com.mycsense.carbondb.MalformedOntologyException;
import com.mycsense.carbondb.NotFoundException;
import com.mycsense.carbondb.domain.CarbonOntology;
import com.mycsense.carbondb.domain.Category;
import com.mycsense.carbondb.domain.Group;

public class CategoryRepo extends AbstractRepo {

    public CategoryRepo(Model model) {
        super(model);
    }

    public Category getCategoriesTree() {
        Category root = new Category();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.CategoryOfGroup);
        while (i.hasNext()) {
            Resource categoryResource = i.next();
            if (!categoryResource.hasProperty(Datatype.belongsToParentCategoryOfGroup)
                    || categoryResource.getProperty(Datatype.belongsToParentCategoryOfGroup) == null
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
        ResIterator i = model.listResourcesWithProperty(Datatype.belongsToParentCategoryOfGroup, categoryResource);
        while (i.hasNext()) {
            Resource subCategoryResource = i.next();
            category.addChild(getCategory(subCategoryResource, category));
        }
        i = model.listResourcesWithProperty(Datatype.belongsToCategoryOfGroup, categoryResource);
        while (i.hasNext()) {
            Resource groupResource = i.next();
            try {
                Group group;
                if (groupResource.hasProperty(RDF.type, Datatype.ProcessGroup)) {
                    group = CarbonOntology.getInstance().getProcessGroup(getId(groupResource));
                } else if (groupResource.hasProperty(RDF.type, Datatype.CoefficientGroup)) {
                    group = CarbonOntology.getInstance().getCoefficientGroup(getId(groupResource));
                } else {
                    throw new MalformedOntologyException("The category " + categoryResource.getURI() + " has a child"
                            + " which is not a process or a coefficient group: " + groupResource.getURI());
                }
                category.addChild(group);
                if (group.hasCategory()) {
                    log.warn("The group " + group.getId() + " is already in the category " + group.getCategory().getId()
                             + " and cannot be added to the category " + category.getId());
                }
                else {
                    group.setCategory(category);
                }
            }
            catch (NotFoundException e) {
                log.error(e.getMessage() + " for the category " + categoryResource.getURI());
            } catch (MalformedOntologyException e) {
                log.error(e.getMessage());
            }
        }

        return category;
    }
}
