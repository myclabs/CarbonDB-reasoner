package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mycsense.carbondb.domain.Category;
import com.mycsense.carbondb.Datatype;

public class CategoryRepo extends AbstractRepo {

    public CategoryRepo(Model model) {
        super(model);
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
            category.addChild(RepoFactory.getGroupRepo().getSimpleGroup(groupResource));
        }

        return category;
    }
}