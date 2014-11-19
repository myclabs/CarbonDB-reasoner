package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.mycsense.carbondb.domain.Unit;

public abstract class AbstractRepo {
    public Model model;

    public AbstractRepo (Model model) {
        this.model = model;
    }

    public String getLabelOrURI(Resource resource)
    {
        StmtIterator iter = resource.listProperties(RDFS.label);
        String label = resource.getURI();
        if (iter.hasNext()) {
            boolean enFound = false;
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                if (s.getLanguage().equals("en")) {
                    label = s.getString();
                    if (enFound) {
                        RepoFactory.getReasonnerReport().addWarning("More than one english label found for resource: " + resource.getURI());
                    }
                    enFound = true;
                }
            }
            if (!enFound) {
                Statement s = resource.getProperty(RDFS.label);
                label = s.getString();
                RepoFactory.getReasonnerReport().addWarning("No english label found for resource: " + resource.getURI() + ", took another language");
            }
        }
        else {
            RepoFactory.getReasonnerReport().addWarning("No label found for resource: " + resource.getURI() + ", took URI");
        }
        return label;
    }

    protected String getUnitURI(Resource element)
    {
        if (element.hasProperty(Datatype.hasUnit) && null != element.getProperty(Datatype.hasUnit)) {
            return element.getProperty(Datatype.hasUnit).getResource().getURI();
        }
        return null;
    }
}
