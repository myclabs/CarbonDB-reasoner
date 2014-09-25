package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public abstract class AbstractRepo {
    public Model model;

    public AbstractRepo (Model model) {
        this.model = model;
    }

    protected String getLabelOrURI(Resource resource)
    {
        StmtIterator iter = resource.listProperties(RDFS.label);
        String label = resource.getURI();
        if (iter.hasNext()) {
            boolean enFound = false;
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                if (s.getLanguage().equals("en")) {
                    label = s.getString();
                    enFound = true;
                }
            }
            if (!enFound) {
                Statement s = resource.getProperty(RDFS.label);
                label = s.getString();
            }
        }
        return label;
    }

    public String getUnit(Resource element)
    {
        if (element.hasProperty(Datatype.hasUnit) && null != element.getProperty(Datatype.hasUnit)) {
            Resource unit = element.getProperty(Datatype.hasUnit).getResource();
            if (unit.hasProperty(Datatype.foreignUnitID) && null != unit.getProperty(Datatype.foreignUnitID)) {
                return unit.getProperty(Datatype.foreignUnitID).getString();
            }
            else {
                //report.addError(element.getURI() + " has no unit");
            }
        }
        return new String();
    }

    protected String getUnitURI(Resource element)
    {
        if (element.hasProperty(Datatype.hasUnit) && null != element.getProperty(Datatype.hasUnit)) {
            return element.getProperty(Datatype.hasUnit).getResource().getURI();
        }
        return null;
    }
}
