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

    public static final Resource Category = resource("Category");
    public static final Resource Relation = resource("Relation");
    public static final Resource SingleProcess = resource("SingleProcess");
    public static final Resource SingleCoefficient = resource("SingleCoefficient");
    public static final Resource CalculateElementaryFlow = resource("CalculatedElementaryFlow");
    public static final Resource ElementaryFlowNature = resource("ElementaryFlowNature");
    public static final Resource Group = resource("Group");
    public static final Resource ProcessGroup = resource("ProcessGroup");
    public static final Resource CoefficientGroup = resource("CoefficientGroup");

    public static final Property hasCategory = property("hasCategory");
    public static final Property hasGroup = property("hasGroup");
    public static final Property hasParent = property("hasParent");
    public static final Property isParentOf = property("isParentOf");
    public static final Property hasWeight = property("hasWeight");
    public static final Property exponent = property("exponent");
    public static final Property hasOrigin = property("hasOrigin");
    public static final Property hasDestination = property("hasDestination");
    public static final Property hasKeyword = property("hasKeyword");
    public static final Property hasTag = property("hasTag");
    public static final Property hasDimension = property("hasDimension");
    public static final Property hasHorizontalDimension = property("hasHorizontalDimension");
    public static final Property hasVerticalDimension = property("hasVerticalDimension");
    public static final Property hasCommonKeyword = property("hasCommonKeyword");
    public static final Property hasDetailedRelation = property("hasDetailedRelation");
    public static final Property hasFlow = property("hasFlow");
    public static final Property hasCalculatedFlow = property("hasCalculatedFlow");
    public static final Property hasNature = property("hasNature");
    public static final Property value = property("value");
    public static final Property uncertainty = property("uncertainty");
    public static final Property involves = property("involves");
    public static final Property hasUnit = property("hasUnit");
    public static final Property foreignUnitID = property("foreignUnitID");

}