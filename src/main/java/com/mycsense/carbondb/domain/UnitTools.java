package com.mycsense.carbondb.domain;

public interface UnitTools {
    public Double getConversionFactor(Unit unit);
    public boolean areCompatible(Unit unit1, Unit unit2);
    public String getUnitsMultiplication(Unit unit1, Unit unit2, int exponent);
    public String getUnitSymbol(String ref);
}
