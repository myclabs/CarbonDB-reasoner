package com.mycsense.carbondb;

public interface UnitsRepo {
    public Double getConversionFactor(String unitID);
    public boolean areCompatible(String unitID1, String unitID2);
    public String getUnitsMultiplication(String unitID1, String unitID2, int exponent);
}
