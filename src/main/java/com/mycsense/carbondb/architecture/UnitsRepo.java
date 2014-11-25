package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.mycsense.carbondb.NoUnitException;
import com.mycsense.carbondb.domain.Unit;

import java.util.HashMap;

public class UnitsRepo extends AbstractRepo {

    protected HashMap<String, Unit> unitsCache;

    public UnitsRepo(Model model) {
        super(model);
        unitsCache = new HashMap<>();
    }

    public Unit getUnit(Resource element) throws NoUnitException {
        if (element.hasProperty(Datatype.hasUnit) && null != element.getProperty(Datatype.hasUnit)) {
            Resource unitResource = element.getProperty(Datatype.hasUnit).getResource();
            String unitId = getId(unitResource);
            if (!unitsCache.containsKey(unitId)) {
                if (unitResource.hasProperty(Datatype.foreignUnitID) && null != unitResource.getProperty(Datatype.foreignUnitID)) {
                    String unitRef = unitResource.getProperty(Datatype.foreignUnitID).getString();
                    unitsCache.put(unitId, new Unit(getId(unitResource), unitRef));
                }
            }
            return unitsCache.get(unitId);
        }
        throw new NoUnitException("The resource " + element.getURI() + " has no unit");
    }
}