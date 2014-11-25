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

import com.mycsense.carbondb.domain.CarbonOntology;
import com.mycsense.carbondb.domain.RelationType;
import com.mycsense.carbondb.domain.SourceRelation;
import com.mycsense.carbondb.domain.relation.Type;

import java.util.ArrayList;
import java.util.HashMap;

public class RelationRepo  extends AbstractRepo {

    protected HashMap<String, SourceRelation> sourceRelationsCache;

    public RelationRepo(Model model) {
        super(model);
        sourceRelationsCache = new HashMap<>();
    }

    public HashMap<String, SourceRelation> getSourceRelations() {
        HashMap<String, SourceRelation> sourceRelations = new HashMap<>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.SourceRelation);
        while (i.hasNext()) {
            sourceRelations.put(getId(i.next()), getSourceRelation(i.next()));
        }

        return sourceRelations;
    }

    public SourceRelation getSourceRelation(Resource sourceRelationResource) {
        if (!sourceRelationsCache.containsKey(sourceRelationResource.getURI())) {
            String originId = getId(sourceRelationResource.getProperty(Datatype.hasOriginProcess).getResource());
            String coeffId = getId(sourceRelationResource.getProperty(Datatype.hasWeightCoefficient).getResource());
            String destinationId = getId(sourceRelationResource.getProperty(Datatype.hasDestinationProcess).getResource());
            SourceRelation sourceRelation = new SourceRelation(
                    CarbonOntology.getInstance().getProcessGroup(getId(originId)),
                    CarbonOntology.getInstance().getCoefficientGroup(getId(coeffId)),
                    CarbonOntology.getInstance().getProcessGroup(getId(destinationId))
            );
            sourceRelation.setId(getId(sourceRelationResource));
            if (sourceRelationResource.hasProperty(Datatype.hasRelationType)
                    && sourceRelationResource.getProperty(Datatype.hasRelationType) != null
                    ) {
                sourceRelation.setType(getRelationType(sourceRelationResource.getProperty(Datatype.hasRelationType).getResource()));
            } else {
                log.warn("The source relation " + sourceRelationResource.getURI() + " has no type");
            }
            if (sourceRelationResource.hasProperty(Datatype.exponent)
                    && null != sourceRelationResource.getProperty(Datatype.exponent)
                    ) {
                sourceRelation.setExponent(sourceRelationResource.getProperty(Datatype.exponent).getInt());
            }
            sourceRelationsCache.put(sourceRelationResource.getURI(), sourceRelation);
        }
        return sourceRelationsCache.get(sourceRelationResource.getURI());
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

    public ArrayList<RelationType> getRelationTypes()
    {
        Selector selector = new SimpleSelector(null, RDF.type, Datatype.RelationType);
        StmtIterator iter = model.listStatements(selector);

        ArrayList<RelationType> types = new ArrayList<>();
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
        String label = relationTypeResource.getProperty(RDFS.label).getString();
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
        return relationType;
    }
}
