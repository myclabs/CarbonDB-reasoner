package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mycsense.carbondb.domain.MacroRelation;

import java.util.ArrayList;

public class RelationRepo  extends AbstractRepo {

    private final UnitsRepo unitsRepo;

    public RelationRepo(Model model, UnitsRepo unitsRepo) {
        super(model);
        this.unitsRepo = unitsRepo;
    }

    public ArrayList<Resource> getMacroRelationsResources() {
        ArrayList<Resource> macroRelations = new ArrayList<>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, Datatype.SourceRelation);
        while (i.hasNext()) {
            macroRelations.add(i.next());
        }

        return macroRelations;
    }

    public ArrayList<MacroRelation> getMacroRelationsForProcessGroup(Resource group) {
        ArrayList<MacroRelation> macroRelations = new ArrayList<>();

        ResIterator i = model.listSubjectsWithProperty(Datatype.involvesElement, group);
        while (i.hasNext()) {
            Resource macroRelationResource = i.next();
            macroRelations.add(getMacroRelation(macroRelationResource));
        }

        return macroRelations;
    }

    public MacroRelation getMacroRelation(Resource macroRelationResource) {
        GroupRepo groupRepo = RepoFactory.getGroupRepo();
        MacroRelation macroRelation = new MacroRelation(
                groupRepo.getGroup(macroRelationResource.getProperty(Datatype.hasOriginProcess).getResource()),
                groupRepo.getGroup(macroRelationResource.getProperty(Datatype.hasWeightCoefficient).getResource()),
                groupRepo.getGroup(macroRelationResource.getProperty(Datatype.hasDestinationProcess).getResource()),
                unitsRepo
        );
        macroRelation.setURI(macroRelationResource.getURI());
        if (macroRelationResource.hasProperty(Datatype.exponent)
                && null != macroRelationResource.getProperty(Datatype.exponent)
                ) {
            macroRelation.setExponent(macroRelationResource.getProperty(Datatype.exponent).getInt());
        }
        return macroRelation;
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

    public void addMicroRelation(Resource sourceProcess, Resource coeff, Resource destinationProcess, int exponent)
    {
        sourceProcess.addProperty(Datatype.hasDetailedRelation,
                model.createResource(Datatype.getURI() + AnonId.create().toString())
                        .addProperty(RDF.type, Datatype.DerivedRelation)
                        .addProperty(Datatype.hasOriginProcess, sourceProcess)
                        .addProperty(Datatype.hasWeightCoefficient, coeff)
                        .addProperty(Datatype.hasDestinationProcess, destinationProcess)
                        .addProperty(Datatype.exponent, model.createTypedLiteral(exponent)));
    }
}
