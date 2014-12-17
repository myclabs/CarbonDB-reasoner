/*
 * Copyright 2014, by Benjamin Bertin and Contributors.
 *
 * This file is part of CarbonDB-reasoner project <http://www.carbondb.org>
 *
 * CarbonDB-reasoner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * CarbonDB-reasoner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CarbonDB-reasoner.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributor(s): -
 *
 */

package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.mycsense.carbondb.NoUnitException;
import com.mycsense.carbondb.UnrecogniedUnitException;
import com.mycsense.carbondb.domain.Unit;

import java.util.HashMap;

public class UnitsRepo extends AbstractRepo {

    protected HashMap<String, Unit> unitsCache;

    public UnitsRepo(Model model) {
        super(model);
        unitsCache = new HashMap<>();
    }

    public Unit getUnit(Resource element) throws NoUnitException, UnrecogniedUnitException {
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