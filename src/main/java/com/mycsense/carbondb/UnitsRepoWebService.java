package com.mycsense.carbondb; 

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.json.JSONArray;

public class UnitsRepoWebService implements UnitsRepo {
    protected HashMap<String, Double> conversionFactors = new HashMap<String, Double>();
    protected HashMap<String, ArrayList<String>> compatibleUnits = new HashMap<String, ArrayList<String>>();

    public Double getConversionFactor(String unitID)
    {
        if (!conversionFactors.containsKey(unitID)) {
            String unitOfReference = findUnitOfReference(unitID);
            if (null == unitOfReference) {
                // unit not found (or error)
                //report.addError("unit not found: " + unitID + " (response status from units API: " + response.getStatus() + ")");
                System.out.println("unit not found: " + unitID);
                conversionFactors.put(unitID, new Double(1.0));
            }
            else if (unitID.equals(unitOfReference)) {
                conversionFactors.put(unitID, new Double(1.0));
            }
            else  {
                conversionFactors.put(unitID, findConversionFactor(unitID, unitOfReference));
            }
        }
        return conversionFactors.get(unitID);
    }

    public boolean areCompatible(String unitID1, String unitID2)
    {
        boolean compatible = false;
        Response response = buildBaseWebTarget()
                .path("compatible")
                .queryParam("unit[0]", unitID1)
                .queryParam("unit[1]", unitID2)
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        if (response.getStatus() == 200) {
            compatible = Boolean.parseBoolean(response.readEntity(String.class));
        }
        return compatible;
    }

    public String getUnitsMultiplication(String unitID1, String unitID2, int exponent)
    {
        String unit = null;
        Response response = buildBaseWebTarget()
                .path("execute")
                .queryParam("operation", "multiplication")
                .queryParam("components[0][unit]", unitID1)
                .queryParam("components[0][exponent]", 1)
                .queryParam("components[1][unit]", unitID2)
                .queryParam("components[1][exponent]", exponent)
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        if (response.getStatus() == 200) {
            JSONObject obj = new JSONObject(response.readEntity(String.class));
            unit = obj.getString("unitId");
        }
        return unit;
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
        Double conversionFactor = new Double(1.0);
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
        ArrayList<String> units = new ArrayList<String>();
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
               .target("http://units.myc-sense.com/api");
    }
}