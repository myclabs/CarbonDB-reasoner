package com.mycsense.carbondb.architecture;

import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import com.mycsense.carbondb.domain.Unit;
import org.json.JSONObject;
import org.json.JSONArray;

public class UnitsRepoWebService implements UnitsRepo, UnitsRepoCache {
    protected HashMap<String, Double> conversionFactorsCache = new HashMap<>();
    protected HashMap<String, HashMap<String, Boolean>> compatibleUnitsCache = new HashMap<>();
    protected HashMap<String, String> symbolsCache = new HashMap<>();
    //protected String unitsAPIURI = "http://units.myc-sense.com/api";
    protected String unitsAPIURI = "http://localhost/units/api";

    public void setUnitsAPIURI(String unitsAPIURI) {
        this.unitsAPIURI = unitsAPIURI;
    }

    public Double getConversionFactor(String unitID)
    {
        if (!conversionFactorsCache.containsKey(unitID)) {
            System.out.println("fetching conversion factor for " + unitID);
            String unitOfReference = findUnitOfReference(unitID);
            if (null == unitOfReference) {
                // unit not found (or error)
                //report.addError("unit not found: " + unitID + " (response status from units API: " + response.getStatus() + ")");
                conversionFactorsCache.put(unitID, 1.0);
            }
            else if (unitID.equals(unitOfReference)) {
                conversionFactorsCache.put(unitID, 1.0);
            }
            else  {
                conversionFactorsCache.put(unitID, findConversionFactor(unitID, unitOfReference));
            }
        }
        return conversionFactorsCache.get(unitID);
    }

    public boolean areCompatible(Unit unit1, Unit unit2)
    {
        return areCompatible(unit1.getRef(), unit2.getRef());
    }

    public boolean areCompatible(Unit unit1, String unitID2)
    {
        return areCompatible(unit1.getRef(), unitID2);
    }

    public boolean areCompatible(String unitID1, String unitID2)
    {
        if (!compatibleUnitsCache.containsKey(unitID1)) {
            compatibleUnitsCache.put(unitID1, new HashMap<String, Boolean>());
        }
        if (!compatibleUnitsCache.get(unitID1).containsKey(unitID2)) {
            System.out.println("fetching compatibility between " + unitID1 + " & " + unitID2);
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

    public String getUnitSymbol(String unitID)
    {
        if (!symbolsCache.containsKey(unitID)) {
            System.out.println("fetching symbol for " + unitID);
            Response response = buildBaseWebTarget()
                    .path("unit")
                    .path(unitID)
                    .request(MediaType.TEXT_PLAIN_TYPE)
                    .get();
            if (response.getStatus() == 200) {
                String responseString = response.readEntity(String.class);
                JSONObject obj = new JSONObject(responseString);
                if (obj.getJSONObject("symbol").isNull("en")
                    || obj.getJSONObject("symbol").get("en").toString().equals("null")
                ) {
                    symbolsCache.put(unitID, unitID);
                }
                else {
                    symbolsCache.put(unitID, obj.getJSONObject("symbol").getString("en"));
                }
            }
            else {
                symbolsCache.put(unitID, unitID);
            }
        }
        return symbolsCache.get(unitID);
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

    protected String findUnitOfReference(String unitID)
    {
        String unitOfReference = null;
        Response response = buildBaseWebTarget()
                .path("unit-of-reference")
                .path(unitID)
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        if (response.getStatus() == 200) {
            JSONObject obj = new JSONObject(response.readEntity(String.class));
            unitOfReference = obj.getString("id");
        }
        return unitOfReference;
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