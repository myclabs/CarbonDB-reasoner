package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.*;
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
            sourceRelations.put(i.next().getURI(), getSourceRelation(i.next()));
        }

        return sourceRelations;
    }

    public SourceRelation getSourceRelation(Resource sourceRelationResource) {
        if (!sourceRelationsCache.containsKey(sourceRelationResource.getURI())) {
            String originURI = sourceRelationResource.getProperty(Datatype.hasOriginProcess).getResource().getURI();
            String coeffURI = sourceRelationResource.getProperty(Datatype.hasWeightCoefficient).getResource().getURI();
            String destinationURI = sourceRelationResource.getProperty(Datatype.hasDestinationProcess).getResource().getURI();
            SourceRelation sourceRelation = new SourceRelation(
                    CarbonOntology.getInstance().getProcessGroup(originURI),
                    CarbonOntology.getInstance().getCoefficientGroup(coeffURI),
                    CarbonOntology.getInstance().getProcessGroup(destinationURI)
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
