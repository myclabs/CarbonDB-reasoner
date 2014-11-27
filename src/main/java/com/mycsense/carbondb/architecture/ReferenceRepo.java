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
                getReference(iter.next().getSubject());
            }
        }
        return refCache;
    }

    protected Reference getReference(Resource referenceResource) {
        if (!refCache.containsKey(referenceResource.getURI())) {
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
