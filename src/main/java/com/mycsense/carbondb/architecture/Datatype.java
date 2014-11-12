package com.mycsense.carbondb.architecture;

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

    protected static Resource resource( String local ) {
        return ResourceFactory.createResource( uri + local );
    }

    protected static Property property( String local ) {
        return ResourceFactory.createProperty( uri, local );
    }

    public static final Resource CategoryOfGroup = resource("CategoryOfGroup");
    public static final Resource CategoryOfImpactType = resource("CategoryOfImpactType");
    public static final Resource CategoryOfElementaryFlowType = resource("CategoryOfElementaryFlowType");
    public static final Resource SourceRelation = resource("SourceRelation");
    public static final Resource DerivedRelation = resource("DerivedRelation");
    public static final Resource SingleProcess = resource("SingleProcess");
    public static final Resource SingleCoefficient = resource("SingleCoefficient");
    public static final Resource CalculateElementaryFlow = resource("CalculatedElementaryFlow");
    public static final Resource ElementaryFlowType = resource("ElementaryFlowType");
    public static final Resource ImpactType = resource("ImpactType");
    public static final Resource Group = resource("Group");
    public static final Resource ProcessGroup = resource("ProcessGroup");
    public static final Resource CoefficientGroup = resource("CoefficientGroup");
    public static final Resource Impact = resource("Impact");
    public static final Resource PositionOfKeywordInDimension = resource("PositionOfKeywordInDimension");
    public static final Resource RelationType = resource("RelationType");
    public static final Resource Asynchronous = resource("Asynchronous");

    public static final Property belongsToCategoryOfGroup = property("belongsToCategoryOfGroup");
    public static final Property belongsToParentCategoryOfGroup = property("belongsToParentCategoryOfGroup");
    public static final Property belongsToCategoryOfElementaryFlowType = property("belongsToCategoryOfElementaryFlowType");
    public static final Property belongsToCategoryOfImpactType = property("belongsToCategoryOfImpactType");
    public static final Property hasWeightCoefficient = property("hasWeightCoefficient");
    public static final Property exponent = property("exponent");
    public static final Property hasOriginProcess = property("hasOriginProcess");
    public static final Property hasDestinationProcess = property("hasDestinationProcess");
    public static final Property containsKeyword = property("containsKeyword");
    public static final Property hasTag = property("hasTag");
    public static final Property hasDimension = property("hasDimension");
    public static final Property hasHorizontalDimension = property("hasHorizontalDimension");
    public static final Property hasVerticalDimension = property("hasVerticalDimension");
    public static final Property hasCommonTag = property("hasCommonTag");
    public static final Property hasDerivedRelation = property("hasDerivedRelation");
    public static final Property hasFlow = property("hasFlow");
    public static final Property hasImpact = property("hasImpact");
    public static final Property hasImpactType = property("hasImpactType");
    public static final Property hasCalculatedFlow = property("hasCalculatedFlow");
    public static final Property hasElementaryFlowType = property("hasElementaryFlowType");
    public static final Property value = property("value");
    public static final Property uncertainty = property("uncertainty");
    public static final Property involvesElement = property("involvesElement");
    public static final Property hasUnit = property("hasUnit");
    public static final Property foreignUnitID = property("foreignUnitID");
    public static final Property hasComponentOfImpactType = property("hasComponentOfImpactType");
    public static final Property isBasedOnElementaryFlowType = property("isBasedOnElementaryFlowType");
    public static final Property hasPositionForSomeKeyword = property("hasPositionForSomeKeyword");
    public static final Property providesPositionToKeyword = property("providesPositionToKeyword");
    public static final Property position = property("position");
    public static final Property hasRelationType = property("hasRelationType");
    public static final Property isDerivedFrom = property("isDerivedFrom");

    // References properties
    public static final Property hasReference = property("hasReference");
    public static final Property creator = property("creator");
    public static final Property date = property("date");
    public static final Property publisher = property("publisher");
    public static final Property source = property("source");
    public static final Property title = property("title");
    public static final Property URL = property("URL");
    public static final Property shortName = property("shortName");
}