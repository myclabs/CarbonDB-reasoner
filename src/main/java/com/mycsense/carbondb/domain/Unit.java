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
            symbol = unitTools.getUnitSymbol(this);
        }
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String toString()
    {
        return getSymbol();
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
