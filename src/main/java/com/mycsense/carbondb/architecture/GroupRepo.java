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
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import com.mycsense.carbondb.MalformedOntologyException;
import com.mycsense.carbondb.NoUnitException;
import com.mycsense.carbondb.UnrecogniedUnitException;
import com.mycsense.carbondb.domain.Dimension;
import com.mycsense.carbondb.domain.DimensionSet;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.dimension.Orientation;
import com.mycsense.carbondb.domain.group.Type;

import java.util.HashMap;

public class GroupRepo extends AbstractRepo {

    public GroupRepo(Model model) {
        super(model);
    }

    public HashMap<String, Group> getProcessGroups() {
        return getGroups(Datatype.ProcessGroup);
    }

    public HashMap<String, Group> getCoefficientGroups() {
        return getGroups(Datatype.CoefficientGroup);
    }

    public HashMap<String, Group> getGroups() {
        return getGroups(Datatype.Group);
    }

    protected HashMap<String, Group> getGroups(Resource groupType) {
        HashMap<String, Group> groups = new HashMap<>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, groupType);
        while (i.hasNext()) {
            Resource resource = i.next();
            try {
                groups.put(getId(resource), getGroup(resource));
            }
            catch (NoUnitException | MalformedOntologyException | UnrecogniedUnitException e) {
                log.error("Unable to load group " + resource.getURI() + ": " + e.getMessage());
            }
        }

        return groups;
    }

    protected Group getGroup(Resource groupResource)
            throws NoUnitException, MalformedOntologyException, UnrecogniedUnitException {
        Group group = new Group(getGroupDimSet(groupResource), getGroupCommonKeywords(groupResource));
        group.setLabel(getLabelOrURI(groupResource));
        group.setId(getId(groupResource));
        group.setUnit(RepoFactory.getUnitsRepo().getUnit(groupResource));
        if (groupResource.hasProperty(RDF.type, Datatype.ProcessGroup)) {
            group.setType(Type.PROCESS);
        }
        else if (groupResource.hasProperty(RDF.type, Datatype.CoefficientGroup)) {
            group.setType(Type.COEFFICIENT);
        }
        else {
            throw new MalformedOntologyException("The group " + groupResource.getURI() + " type is not correct");
        }
        if (groupResource.hasProperty(RDFS.comment) && groupResource.getProperty(RDFS.comment) != null) {
            group.setComment(groupResource.getProperty(RDFS.comment).getString());
        }
        group.setReferences(RepoFactory.getReferenceRepo().getReferencesForResource(groupResource));
        return group;
    }

    protected DimensionSet getGroupDimSet(Resource groupResource) {
        Selector selector = new SimpleSelector(groupResource, Datatype.hasDimension, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        DimensionSet dimSet = new DimensionSet();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                Resource dimensionResource = s.getObject().asResource();
                Dimension dim = getDimensionKeywords(dimensionResource);
                if (groupResource.hasProperty(Datatype.hasHorizontalDimension, dimensionResource)) {
                    dim.setOrientation(Orientation.HORIZONTAL);
                }
                else if (groupResource.hasProperty(Datatype.hasVerticalDimension, dimensionResource)) {
                    dim.setOrientation(Orientation.VERTICAL);
                }
                dimSet.add(dim);
            }
        }
        return dimSet;
    }

    protected Dimension getGroupCommonKeywords(Resource groupResource) {
        Selector selector = new SimpleSelector(groupResource, Datatype.hasCommonTag, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        Dimension dim = new Dimension();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                dim.addKeyword(RepoFactory.getKeywordRepo().getKeyword(s.getObject().asResource()));
            }
        }
        return dim;
    }

    protected Dimension getDimensionKeywords(Resource dimensionResource) {
        Selector selector = new SimpleSelector(dimensionResource, Datatype.containsKeyword, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        Dimension dim = new Dimension();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                dim.addKeyword(RepoFactory.getKeywordRepo().getKeyword(s.getObject().asResource()));
            }
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
