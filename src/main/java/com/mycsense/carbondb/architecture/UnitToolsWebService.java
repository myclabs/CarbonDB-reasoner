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

import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import com.mycsense.carbondb.NotFoundException;
import com.mycsense.carbondb.domain.Unit;
import com.mycsense.carbondb.domain.UnitTools;
import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitToolsWebService implements UnitTools {
    protected HashMap<String, Double> conversionFactorsCache = new HashMap<>();
    protected HashMap<String, HashMap<String, Boolean>> compatibleUnitsCache = new HashMap<>();
    protected HashMap<String, String> symbolsCache = new HashMap<>();
    //protected String unitsAPIURI = "http://units.myc-sense.com/api";
    protected String unitsAPIURI = "http://localhost/units/api";

    public void setUnitsAPIURI(String unitsAPIURI) {
        this.unitsAPIURI = unitsAPIURI;
    }

    private final Logger log = LoggerFactory.getLogger(UnitToolsWebService.class);

    public Double getConversionFactor(Unit unit)
    {
        String unitID = unit.getRef();
        if (!conversionFactorsCache.containsKey(unitID)) {
            log.debug("fetching conversion factor for " + unitID);
            try {
                String unitOfReference = findUnitOfReference(unitID);
                if (unitID.equals(unitOfReference)) {
                    conversionFactorsCache.put(unitID, 1.0);
                }
                else  {
                    conversionFactorsCache.put(unitID, findConversionFactor(unitID, unitOfReference));
                }
            }
            catch (NotFoundException e) {
                log.warn(e.getMessage());
                conversionFactorsCache.put(unitID, 1.0);
            }
        }
        return conversionFactorsCache.get(unitID);
    }

    public boolean areCompatible(Unit unit1, Unit unit2)
    {
        String unitID1 = unit1.getRef(), unitID2 = unit2.getRef();
        if (!compatibleUnitsCache.containsKey(unitID1)) {
            compatibleUnitsCache.put(unitID1, new HashMap<String, Boolean>());
        }
        if (!compatibleUnitsCache.get(unitID1).containsKey(unitID2)) {
            log.debug("fetching compatibility between " + unitID1 + " & " + unitID2);
            Response response = buildBaseWebTarget()
                    .path("compatible")
                    .queryParam("units[0]", unitID1)
                    .queryParam("units[1]", unitID2)
                    .request(MediaType.TEXT_PLAIN_TYPE)
                    .get();
            String result = response.readEntity(String.class);
            if (response.getStatus() == 200) {
                compatibleUnitsCache.get(unitID1).put(unitID2, Boolean.parseBoolean(result));
            }
            else {
                log.warn("Unable to find unit compatibility with unitID1: " + unitID1 + " and unitID2: " + unitID2
                         + " - units are considered incompatibles");
                compatibleUnitsCache.get(unitID1).put(unitID2, false);
            }
        }
        return compatibleUnitsCache.get(unitID1).get(unitID2);
    }

    public String getUnitsMultiplication(Unit unit1, Unit unit2, int exponent)
    {
        String unit = null;
        Response response = buildBaseWebTarget()
                .path("execute")
                .queryParam("operation", "multiplication")
                .queryParam("components[0][unit]", unit1.getRef())
                .queryParam("components[0][exponent]", 1)
                .queryParam("components[1][unit]", unit2.getRef())
                .queryParam("components[1][exponent]", exponent)
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        if (response.getStatus() == 200) {
            JSONObject obj = new JSONObject(response.readEntity(String.class));
            unit = obj.getString("unitId");
        }
        return unit;
    }

    public String getUnitSymbol(String ref)
    {
        if (!symbolsCache.containsKey(ref)) {
            log.debug("fetching symbol for " + ref);
            Response response = buildBaseWebTarget()
                    .path("unit")
                    .path(ref)
                    .request(MediaType.TEXT_PLAIN_TYPE)
                    .get();
            if (response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                JSONObject obj = new JSONObject(responseString);
                if (obj.getJSONObject("symbol").isNull("en")
                        || obj.getJSONObject("symbol").get("en").toString().equals("null")
                        ) {
                    symbolsCache.put(ref, ref);
                }
                else {
                    symbolsCache.put(ref, obj.getJSONObject("symbol").getString("en"));
                }
            }
            else {
                symbolsCache.put(ref, ref);
            }
        }
        return symbolsCache.get(ref);
    }

    public HashMap<String, Double> getConversionFactorsCache() {
        return conversionFactorsCache;
    }

    public void setConversionFactorsCache(HashMap<String, Double> conversionFactorsCache) {
        this.conversionFactorsCache = conversionFactorsCache;
    }

    public HashMap<String, HashMap<String, Boolean>> getCompatibleUnitsCache() {
        return compatibleUnitsCache;
    }

    public void setCompatibleUnitsCache(HashMap<String, HashMap<String, Boolean>> compatibleUnitsCache) {
        this.compatibleUnitsCache = compatibleUnitsCache;
    }

    public HashMap<String, String> getSymbolsCache() {
        return symbolsCache;
    }

    public void setSymbolsCache(HashMap<String, String> symbolsCache) {
        this.symbolsCache = symbolsCache;
    }

    protected String findUnitOfReference(String unitID) throws NotFoundException {
        Response response = buildBaseWebTarget()
                .path("unit-of-reference")
                .path(unitID)
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        if (response.getStatus() != 200) {
            throw new NotFoundException("Unit not found: " + unitID
                    + " (response status from units API: " + response.getStatus() + ")");
        }
        JSONObject obj = new JSONObject(response.readEntity(String.class));
        return obj.getString("id");
    }

    protected Double findConversionFactor(String sourceUnitID, String destinationUnitID)
    {
        Double conversionFactor = 1.0;
        Response response = buildBaseWebTarget()
                .path("conversion-factor")
                .queryParam("unit2", sourceUnitID)
                .queryParam("unit1", destinationUnitID)
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        if (response.getStatus() == 200) {
            conversionFactor = new Double(response.readEntity(String.class));
        }
        return conversionFactor;
    }

    protected ArrayList<String> findCompatibleUnits(String unitID)
    {
        ArrayList<String> units = new ArrayList<>();
        Response response = buildBaseWebTarget()
                .path("compatible-units")
                .path(unitID)
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        if (response.getStatus() == 200) {
            JSONArray arr = new JSONArray(response.readEntity(String.class));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                units.add(obj.getString("id"));
            }
        }
        return units;
    }

    protected WebTarget buildBaseWebTarget()
    {
        return ClientBuilder.newClient()
                .target(unitsAPIURI);
    }
}
