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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRepo {
    public Model model;

    protected final Logger log = LoggerFactory.getLogger(AbstractRepo.class);

    public AbstractRepo (Model model) {
        this.model = model;
    }

    public String getLabelOrURI(Resource resource)
    {
        StmtIterator iter = resource.listProperties(RDFS.label);
        String label = resource.getURI();
        if (iter.hasNext()) {
            boolean enFound = false;
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                if (s.getLanguage().equals("en")) {
                    label = s.getString();
                    if (enFound) {
                        log.warn("More than one english label found for resource: " + resource.getURI());
                    }
                    enFound = true;
                }
            }
            if (!enFound) {
                Statement s = resource.getProperty(RDFS.label);
                label = s.getString();
                log.warn("No english label found for resource: " + resource.getURI() + ", took another language");
            }
        }
        else {
            log.warn("No label found for resource: " + resource.getURI() + ", took URI");
        }
        return label;
    }

    public Double getUncertainty(Resource resource)
    {
        if (resource.hasProperty(Datatype.uncertainty) && null != resource.getProperty(Datatype.uncertainty)) {
            return resource.getProperty(Datatype.uncertainty).getDouble();
        }
        //log.warn("The resource " + resource.getURI() + " has no uncertainty, using 0.0 instead"); // deactivated for a while
        return 0.0;
    }

    public Double getValue(Resource resource)
    {
        if (resource.hasProperty(Datatype.value) && null != resource.getProperty(Datatype.value)) {
            return resource.getProperty(Datatype.value).getDouble();
        }
        log.warn("The resource " + resource.getURI() + " has no value, using 0.0 instead");
        return 0.0;
    }

    protected String getId(Resource resource)
    {
        return getId(resource.getURI());
    }

    protected String getId(String uri)
    {
        return uri.replace(Datatype.getURI(), "");
    }
}
