package com.mycsense.carbondb.architecture;

import com.mycsense.carbondb.domain.Unit;

public interface UnitsRepo {
    public Double getConversionFactor(String unitID);
    public boolean areCompatible(String unitID1, String unitID2);
    public boolean areCompatible(Unit unitID1, Unit unitID2);
    public boolean areCompatible(Unit unitID1, String unitID2);
    public String getUnitsMultiplication(Unit unit1, Unit unit2, int exponent);
    public String getUnitSymbol(String unitID);
}
