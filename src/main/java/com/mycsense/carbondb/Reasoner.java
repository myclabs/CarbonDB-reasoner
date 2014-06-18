package com.mycsense.carbondb; 

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.InfModel;
import org.mindswap.pellet.jena.PelletReasonerFactory;

public class Reasoner {

    protected Model model;
    protected InfModel infModel;
    protected com.hp.hpl.jena.reasoner.Reasoner jenaReasoner;

    Reasoner (Model model) {
        this.model = model;
        jenaReasoner = PelletReasonerFactory.theInstance().create();
    }

    public void run () {
        /*
        load the ontology -> ontologyloader
        convert macro relations -> macrorelations convert
        calculate ecological flows -> calculate ecological flows
        */
        infModel = ModelFactory.createInfModel( jenaReasoner, model );
        Conversion conv = new Conversion(infModel);
        conv.run();
        Calculation calc = new Calculation(infModel);
        calc.run();
    }

    public InfModel getInfModel() {
        return infModel;
    }

    public Model getModel() {
        return model;
    }
}