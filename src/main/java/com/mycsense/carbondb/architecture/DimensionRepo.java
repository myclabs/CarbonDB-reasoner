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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mycsense.carbondb.EmptyDimensionException;
import com.mycsense.carbondb.domain.Dimension;

import java.util.HashMap;

public class DimensionRepo extends AbstractRepo {

    public DimensionRepo(Model model) {
        super(model);
    }

    protected HashMap<String, Dimension> getDimensions(Resource groupType) {
        HashMap<String, Dimension> dimensions = new HashMap<>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, groupType);
        while (i.hasNext()) {
            Resource resource = i.next();
            try {
                dimensions.put(getId(resource), getDimension(resource));
            }
            catch (EmptyDimensionException e) {
                log.error("Unable to load dimension " + resource.getURI() + ": " + e.getMessage());
            }
        }

        return dimensions;
    }

    protected Dimension getDimension(Resource dimensionResource) throws EmptyDimensionException {
        Selector selector = new SimpleSelector(dimensionResource, Datatype.containsKeyword, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        Dimension dim = new Dimension(getId(dimensionResource));
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                dim.addKeyword(RepoFactory.getKeywordRepo().getKeyword(s.getObject().asResource()));
            }
        }
        else {
            throw new EmptyDimensionException("The dimension " + dimensionResource.getURI()
                                              + " does not contain any keyword");
        }
        setKeywordsPositionForDimension(dimensionResource, dim);
        return dim;
    }

    protected void setKeywordsPositionForDimension(Resource dimensionResource, Dimension dimension) {
        NodeIterator i = model.listObjectsOfProperty(dimensionResource, Datatype.hasPositionAsDimensionForSomeKeyword);
        while (i.hasNext()) {
            Resource positionResource = i.next().asResource();
            if (positionResource.hasProperty(Datatype.position)
                    && positionResource.getProperty(Datatype.position) != null
                    && positionResource.hasProperty(Datatype.providesPositionToKeywordInSomeDimension)
                    && positionResource.getProperty(Datatype.providesPositionToKeywordInSomeDimension) != null
                    ) {
                dimension.addKeywordPosition(
                        positionResource.getProperty(Datatype.position).getInt(),
                        getId(positionResource.getPropertyResourceValue(Datatype.providesPositionToKeywordInSomeDimension))
                );
            }
        }
    }
}
