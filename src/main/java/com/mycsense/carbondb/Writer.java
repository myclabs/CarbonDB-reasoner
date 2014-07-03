package com.mycsense.carbondb; 

import java.io.InputStream;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.util.FileManager;

import java.util.Iterator;
import java.util.List;

public class Writer {

    public Model model;

    public Writer (Model model) {
        this.model = model;
    }

    public void addMicroRelation(Resource sourceProcess, Resource coeff, Resource destinationProcess)
    {
        sourceProcess.addProperty(Datatype.hasDetailedRelation,
            model.createResource(Datatype.getURI() + AnonId.create().toString())
            .addProperty(RDF.type, Datatype.Relation)
            .addProperty(Datatype.hasOrigin, sourceProcess)
            .addProperty(Datatype.hasWeight, coeff)
            .addProperty(Datatype.hasDestination, destinationProcess));
    }

    public void addCumulatedEcologicalFlow(Resource process, Resource elementaryFlowNature, float value)
    {
        process.addProperty(Datatype.emits,
            model.createResource(Datatype.getURI() + AnonId.create().toString())
            .addProperty(Datatype.hasNature, elementaryFlowNature)
            .addProperty(Datatype.value, model.createTypedLiteral(value))
            .addProperty(RDF.type, Datatype.CalculateElementaryFlow));          
    }
}