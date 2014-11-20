package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.mycsense.carbondb.domain.Unit;

public class UnitsRepo extends AbstractRepo {

    public UnitsRepo(Model model) {
        super(model);
    }

    public Unit getUnit(Resource element)
    {
        if (element.hasProperty(Datatype.hasUnit) && null != element.getProperty(Datatype.hasUnit)) {
            Resource unitResource = element.getProperty(Datatype.hasUnit).getResource();
            if (unitResource.hasProperty(Datatype.foreignUnitID) && null != unitResource.getProperty(Datatype.foreignUnitID)) {
                String unitRef = unitResource.getProperty(Datatype.foreignUnitID).getString();
                Unit unit = new Unit(
                    unitResource.getURI(),
                    unitRef
                );
                return unit;
            }
            else {
                //report.addError(element.getURI() + " has no unit");
            }
        }
        // @todo this method should throw an exception or return an empty unit if the given element has no unit
        return null;
    }
}