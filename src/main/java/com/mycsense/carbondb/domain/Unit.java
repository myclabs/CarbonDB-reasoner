package com.mycsense.carbondb.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Unit {
    protected String id;
    protected String symbol;
    protected String ref;

    protected static UnitTools unitTools;

    public static void setUnitTools(UnitTools pUnitTools) {
        unitTools = pUnitTools;
    }

    public Unit(String id, String ref) {
        this.id = id;
        this.ref = ref;
    }

    public Unit(String id, String symbol, String ref) {
        this.id = id;
        this.symbol = symbol;
        this.ref = ref;
    }

    public boolean isCompatible(Unit unit) {
        return unitTools.areCompatible(this, unit);
    }

    public Unit multiply(Unit unit, int exponent) {
        return new Unit(null, unitTools.getUnitsMultiplication(this, unit, exponent));
    }

    public Double getConversionFactor() {
        return unitTools.getConversionFactor(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbol() {
        if (null == symbol) {
            symbol = unitTools.getUnitSymbol(ref);
        }
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String toString()
    {
        return symbol;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public boolean equals(Object obj) {
        // Alternative: use Guava (from Google)
        if (!(obj instanceof Unit))
            return false;
        if (obj == this)
            return true;

        Unit rhs = (Unit) obj;
        return new EqualsBuilder()
                .append(id, rhs.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 313)
                .append(id)
                .toHashCode();
    }
}
