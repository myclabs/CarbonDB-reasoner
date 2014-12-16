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

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import com.mycsense.carbondb.MalformedOntologyException;
import com.mycsense.carbondb.NotFoundException;
import com.mycsense.carbondb.domain.CarbonOntology;
import com.mycsense.carbondb.domain.RelationType;
import com.mycsense.carbondb.domain.SourceRelation;
import com.mycsense.carbondb.domain.relation.Type;

import java.util.HashMap;
import java.util.HashSet;

public class RelationRepo  extends AbstractRepo {

    protected HashMap<String, SourceRelation> sourceRelationsCache;
    protected HashMap<String, RelationType> relationTypesCache;

    public RelationRepo(Model model) {
        super(model);
        sourceRelationsCache = new HashMap<>();
        relationTypesCache = new HashMap<>();
    }

    public HashMap<String, SourceRelation> getSourceRelations() {
        HashMap<String, SourceRelation> sourceRelations = new HashMap<>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.SourceRelation);
        while (i.hasNext()) {
            Resource relationResource = i.next();
            try {
                sourceRelations.put(getId(relationResource), getSourceRelation(relationResource));
            } catch (NotFoundException | MalformedOntologyException e) {
                log.error("Unable to load source relation " + relationResource.getURI() + ": " + e.getMessage());
            }
        }

        return sourceRelations;
    }

    protected SourceRelation getSourceRelation(Resource sourceRelationResource) throws NotFoundException, MalformedOntologyException {
        if (!sourceRelationResource.hasProperty(Datatype.hasOriginProcess)
            || sourceRelationResource.getProperty(Datatype.hasOriginProcess) == null
        ) {
            throw new MalformedOntologyException("Missing " + Datatype.hasOriginProcess + " property");
        }
        if (!sourceRelationResource.hasProperty(Datatype.hasWeightCoefficient)
            || sourceRelationResource.getProperty(Datatype.hasWeightCoefficient) == null
        ) {
            throw new MalformedOntologyException("Missing " + Datatype.hasWeightCoefficient + " property");
        }
        if (!sourceRelationResource.hasProperty(Datatype.hasDestinationProcess)
            || sourceRelationResource.getProperty(Datatype.hasDestinationProcess) == null
        ) {
            throw new MalformedOntologyException("Missing " + Datatype.hasDestinationProcess + " property");
        }
        String originId = getId(sourceRelationResource.getProperty(Datatype.hasOriginProcess).getResource());
        String coeffId = getId(sourceRelationResource.getProperty(Datatype.hasWeightCoefficient).getResource());
        String destinationId = getId(sourceRelationResource.getProperty(Datatype.hasDestinationProcess).getResource());
        SourceRelation sourceRelation = new SourceRelation(
                CarbonOntology.getInstance().getProcessGroup(getId(originId)),
                CarbonOntology.getInstance().getCoefficientGroup(getId(coeffId)),
                CarbonOntology.getInstance().getProcessGroup(getId(destinationId))
        );
        sourceRelation.setId(getId(sourceRelationResource));
        if (!sourceRelationResource.hasProperty(Datatype.hasRelationType)
            || sourceRelationResource.getProperty(Datatype.hasRelationType) == null
        ) {
            throw new MalformedOntologyException("Missing " + Datatype.hasRelationType + " property");
        }
        sourceRelation.setType(getRelationType(sourceRelationResource.getProperty(Datatype.hasRelationType).getResource()));
        if (!sourceRelationResource.hasProperty(Datatype.exponent)
            || sourceRelationResource.getProperty(Datatype.exponent) == null
        ) {
            log.warn("The source relation " + sourceRelationResource.getURI() + " has no exponent, using default value 1.0");
        }
        else {
            sourceRelation.setExponent(sourceRelationResource.getProperty(Datatype.exponent).getInt());
        }
        return sourceRelation;
    }

    public void addDerivedRelation(Resource sourceProcess, Resource coeff, Resource destinationProcess, int exponent, Resource sourceRelation)
    {
        sourceProcess.addProperty(Datatype.hasDerivedRelation,
                model.createResource(Datatype.getURI() + AnonId.create().toString())
                        .addProperty(RDF.type, Datatype.DerivedRelation)
                        .addProperty(Datatype.isDerivedFrom, sourceRelation)
                        .addProperty(Datatype.hasOriginProcess, sourceProcess)
                        .addProperty(Datatype.hasWeightCoefficient, coeff)
                        .addProperty(Datatype.hasDestinationProcess, destinationProcess)
                        .addProperty(Datatype.exponent, model.createTypedLiteral(exponent)));
    }

    public HashSet<RelationType> getRelationTypes()
    {
        Selector selector = new SimpleSelector(null, RDF.type, Datatype.RelationType);
        StmtIterator iter = model.listStatements(selector);

        HashSet<RelationType> types = new HashSet<>();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                types.add(getRelationType(s.getSubject()));
            }
        }
        return types;
    }

    public RelationType getRelationType(Resource relationTypeResource)
    {
        if (!relationTypesCache.containsKey(relationTypeResource.getURI())) {
            String label = getLabelOrURI(relationTypeResource);
            Type type = Type.SYNCHRONOUS;
            if (relationTypeResource.hasProperty(RDF.type, Datatype.Asynchronous)) {
                type = Type.ASYNCHRONOUS;
            }
            RelationType relationType = new RelationType(getId(relationTypeResource), label, type);
            if (relationTypeResource.hasProperty(RDFS.comment)
                && relationTypeResource.getProperty(RDFS.comment) != null
            ) {
                relationType.setComment(relationTypeResource.getProperty(RDFS.comment).getString());
            }
            relationTypesCache.put(relationTypeResource.getURI(), relationType);
        }
        return relationTypesCache.get(relationTypeResource.getURI());
    }
}
