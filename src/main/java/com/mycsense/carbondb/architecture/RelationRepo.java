package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mycsense.carbondb.domain.SourceRelation;

import java.util.ArrayList;

public class RelationRepo  extends AbstractRepo {

    private final UnitsRepo unitsRepo;

    public RelationRepo(Model model, UnitsRepo unitsRepo) {
        super(model);
        this.unitsRepo = unitsRepo;
    }

    public ArrayList<Resource> getSourceRelationsResources() {
        ArrayList<Resource> sourceRelations = new ArrayList<>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.SourceRelation);
        while (i.hasNext()) {
            sourceRelations.add(i.next());
        }

        return sourceRelations;
    }

    public ArrayList<SourceRelation> getSourceRelationsForProcessGroup(Resource group) {
        ArrayList<SourceRelation> sourceRelations = new ArrayList<>();

        ResIterator i = model.listSubjectsWithProperty(Datatype.involvesElement, group);
        while (i.hasNext()) {
            Resource sourceRelationResource = i.next();
            sourceRelations.add(getSourceRelation(sourceRelationResource));
        }

        return sourceRelations;
    }

    public SourceRelation getSourceRelation(Resource sourceRelationResource) {
        GroupRepo groupRepo = RepoFactory.getGroupRepo();
        SourceRelation sourceRelation = new SourceRelation(
                groupRepo.getGroup(sourceRelationResource.getProperty(Datatype.hasOriginProcess).getResource()),
                groupRepo.getGroup(sourceRelationResource.getProperty(Datatype.hasWeightCoefficient).getResource()),
                groupRepo.getGroup(sourceRelationResource.getProperty(Datatype.hasDestinationProcess).getResource()),
                unitsRepo
        );
        sourceRelation.setURI(sourceRelationResource.getURI());
        if (sourceRelationResource.hasProperty(Datatype.exponent)
                && null != sourceRelationResource.getProperty(Datatype.exponent)
                ) {
            sourceRelation.setExponent(sourceRelationResource.getProperty(Datatype.exponent).getInt());
        }
        return sourceRelation;
    }

    public Double getCoefficientValueForRelation(Resource relation) {
        Resource coefficient = relation.getProperty(Datatype.hasWeightCoefficient).getResource();
        Double value = coefficient.getProperty(Datatype.value).getDouble();
        int exponent = relation.getProperty(Datatype.exponent).getInt();
        String unitID = getUnit(coefficient);
        value *= unitsRepo.getConversionFactor(unitID);
        if (-1 == exponent) {
            value = 1 / value;
        }
        return value;
    }

    public Double getCoefficientUncertaintyForRelation(Resource relation) {
        Resource coefficient = relation.getProperty(Datatype.hasWeightCoefficient).getResource();
        return RepoFactory.getSingleElementRepo().getUncertainty(coefficient);
    }

    public ArrayList<Resource> getRelationsForProcess(Resource process) {
        Selector selector = new SimpleSelector(null, Datatype.hasOriginProcess, process);
        StmtIterator iter = model.listStatements(selector);

        ArrayList<Resource> relations = new ArrayList<>();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                relations.add(s.getSubject());
            }
        }
        return relations;
    }

    public void addDerivedRelation(Resource sourceProcess, Resource coeff, Resource destinationProcess, int exponent)
    {
        sourceProcess.addProperty(Datatype.hasDerivedRelation,
                model.createResource(Datatype.getURI() + AnonId.create().toString())
                        .addProperty(RDF.type, Datatype.DerivedRelation)
                        .addProperty(Datatype.hasOriginProcess, sourceProcess)
                        .addProperty(Datatype.hasWeightCoefficient, coeff)
                        .addProperty(Datatype.hasDestinationProcess, destinationProcess)
                        .addProperty(Datatype.exponent, model.createTypedLiteral(exponent)));
    }
}
