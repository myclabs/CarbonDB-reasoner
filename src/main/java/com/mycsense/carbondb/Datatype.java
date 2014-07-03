package com.mycsense.carbondb;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Property;


/**
 *   The CarbonDB ontology vocabulary.
 */

public class Datatype {

    protected static final String uri ="http://www.myc-sense.com/ontologies/bc#";

    /** returns the URI for this schema
     *  @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }

    protected static final Resource resource( String local ) {
        return ResourceFactory.createResource( uri + local );
    }

    protected static final Property property( String local ) {
        return ResourceFactory.createProperty( uri, local );
    }

    public static final Resource Relation = resource("Relation");
    public static final Resource SingleProcess = resource("SingleProcess");
    public static final Resource SingleCoefficient = resource("SingleCoefficient");
    public static final Resource CalculateElementaryFlow = resource("CalculatedElementaryFlow");
    public static final Resource ElementaryFlowNature = resource("ElementaryFlowNature");
    public static final Resource Processfamily = resource("Processfamily");

    public static final Property hasWeight = property("hasWeight");
    public static final Property hasOrigin = property("hasOrigin");
    public static final Property hasDestination = property("hasDestination");
    public static final Property hasKeyword = property("hasKeyword");
    public static final Property hasDimension = property("hasDimension");
    public static final Property hasDetailedRelation = property("hasDetailedRelation");
    public static final Property emits = property("emits");
    public static final Property hasNature = property("hasNature");
    public static final Property value = property("value");

}