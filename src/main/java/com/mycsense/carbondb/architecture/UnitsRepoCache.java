package com.mycsense.carbondb.architecture;

import java.util.HashMap;

public interface UnitsRepoCache {
    public HashMap<String, Double> getConversionFactorsCache();
    public void setConversionFactorsCache(HashMap<String, Double> conversionFactorsCache);
    public HashMap<String, HashMap<String, Boolean>> getCompatibleUnitsCache();
    public void setCompatibleUnitsCache(HashMap<String, HashMap<String, Boolean>> compatibleUnitsCache);
}
