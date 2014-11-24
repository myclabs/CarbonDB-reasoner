package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRepo {
    public Model model;

    protected final Logger log = LoggerFactory.getLogger(UnitToolsWebService.class);

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

    public Double getUncertainty(Resource resource)
    {
        if (resource.hasProperty(Datatype.uncertainty) && null != resource.getProperty(Datatype.uncertainty)) {
            return resource.getProperty(Datatype.uncertainty).getDouble();
        }
        return 0.0;
    }

    protected String getId(Resource resource)
    {
        return getId(resource.getURI());
    }

    protected String getId(String uri)
    {
        return uri.replace(Datatype.getURI(), "");
    }
}
