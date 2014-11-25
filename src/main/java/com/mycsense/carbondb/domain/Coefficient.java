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

import java.util.HashSet;

public class Coefficient extends SingleElement {
    protected Value value;
    protected HashSet<DerivedRelation> derivedRelations;

    public Coefficient(Dimension keywords, Unit unit, Value value) {
        super(keywords, unit);
        this.value = value;
        derivedRelations = new HashSet<>();
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public void addDerivedRelation(DerivedRelation derivedRelation) {
        derivedRelations.add(derivedRelation);
    }

    public HashSet<DerivedRelation> getDerivedRelations() {
        return derivedRelations;
    }
}
