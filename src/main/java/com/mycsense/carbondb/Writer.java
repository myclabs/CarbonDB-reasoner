package com.mycsense.carbondb; 

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

public class Writer {

    public Model model;

    public Writer (Model model) {
        this.model = model;
    }

    public void addMicroRelation(Resource sourceProcess, Resource coeff, Resource destinationProcess, int exponent)
    {
        sourceProcess.addProperty(Datatype.hasDetailedRelation,
            model.createResource(Datatype.getURI() + AnonId.create().toString())
            .addProperty(RDF.type, Datatype.DetailedRelation)
            .addProperty(Datatype.hasOriginProcess, sourceProcess)
            .addProperty(Datatype.hasWeightCoefficient, coeff)
            .addProperty(Datatype.hasDestinationProcess, destinationProcess)
            .addProperty(Datatype.exponent, model.createTypedLiteral(exponent)));
    }

    public void addCumulatedEcologicalFlow(Resource process, Resource elementaryFlowNature, double value, double uncertainty)
    {
        process.addProperty(Datatype.hasCalculatedFlow,
            model.createResource(Datatype.getURI() + AnonId.create().toString())
            .addProperty(Datatype.hasElementaryFlowType, elementaryFlowNature)
            .addProperty(Datatype.value, model.createTypedLiteral(value))
            .addProperty(Datatype.uncertainty, model.createTypedLiteral(uncertainty))
            .addProperty(RDF.type, Datatype.CalculateElementaryFlow));
    }

    public Resource createProcess(Dimension dimension, String unitURI)
    {
        Resource process = model.createResource(Datatype.getURI() + AnonId.create().toString())
                            .addProperty(RDF.type, Datatype.SingleProcess);
        if (null != unitURI) {
            process.addProperty(Datatype.hasUnit, model.createResource(unitURI));
        }
        for (Keyword keyword: dimension.keywords) {
            Resource keywordResource = model.createResource(keyword.name);
            process.addProperty(Datatype.hasTag, keywordResource);
        }
        return process;
    }
}