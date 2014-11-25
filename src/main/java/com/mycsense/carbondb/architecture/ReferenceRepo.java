package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mycsense.carbondb.domain.Reference;

import java.util.ArrayList;
import java.util.HashMap;

public class ReferenceRepo extends AbstractRepo {

    protected HashMap<String, Reference> refCache;

    public ReferenceRepo(Model model) {
        super(model);
        refCache = new HashMap<>();
    }

    public ArrayList<Reference> getReferencesForResource(Resource resource) {
        ArrayList<Reference> references = new ArrayList<>();

        StmtIterator i = resource.listProperties(Datatype.hasReference);
        while (i.hasNext()) {
            Statement stmt = i.next();
            references.add(getReference(stmt.getResource()));
        }

        return references;
    }

    public HashMap<String, Reference> getReferences() {
        Selector selector = new SimpleSelector(null, RDF.type, Datatype.Reference);
        StmtIterator iter = model.listStatements(selector);

        if (iter.hasNext()) {
            while (iter.hasNext()) {
                getReference(iter.nextStatement().getResource());
            }
        }
        return refCache;
    }

    protected Reference getReference(Resource referenceResource) {
        if (refCache.containsKey(referenceResource.getURI())) {
            String title = getStringForProperty(referenceResource, Datatype.title),
                   source = getStringForProperty(referenceResource, Datatype.source),
                   URL = getStringForProperty(referenceResource, Datatype.URL),
                   creator = getStringForProperty(referenceResource, Datatype.creator),
                   publisher = getStringForProperty(referenceResource, Datatype.publisher),
                   date = getStringForProperty(referenceResource, Datatype.date),
                   shortName = getStringForProperty(referenceResource, Datatype.shortName);

            refCache.put(referenceResource.getURI(), new Reference(title, source, URL, creator, publisher, date, referenceResource.getURI(), shortName));
        }
        return refCache.get(referenceResource.getURI());
    }

    protected String getStringForProperty(Resource resource, Property property) {
        if (resource.hasProperty(property)
                && resource.getProperty(property) != null) {
            return resource.getProperty(property).getString();
        }
        return null;
    }

}
