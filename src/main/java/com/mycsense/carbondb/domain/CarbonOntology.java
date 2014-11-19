package com.mycsense.carbondb.domain;

import com.mycsense.carbondb.NotFoundException;

import java.util.HashMap;

public class CarbonOntology {
    HashMap<String, Group> processGroups;
    HashMap<String, Group> coefficientGroups;
    HashMap<String, Process> processes;
    HashMap<String, Coefficient> coefficients;
    HashMap<String, RelationType> relationTypes;
    HashMap<String, Reference> references;
    HashMap<String, SourceRelation> sourceRelations;
    HashMap<String, DerivedRelation> derivedRelations;
    HashMap<String, ElementaryFlowType> elementaryFlowTypes;

    protected ElementaryFlowType getElementaryFlowType(String uri) throws NotFoundException {
        if (!elementaryFlowTypes.containsKey(uri)) {
            throw new NotFoundException("The elementary flow type with the uri " + uri + " was not found");
        }
        return elementaryFlowTypes.get(uri);
    }
}
