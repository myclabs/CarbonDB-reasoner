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

public class TypeCategory extends Category {
    protected Unit unit;

    public TypeCategory(Unit unit) {
        this.unit = unit;
    }

    public TypeCategory(String id, Unit unit) {
        super(id);
        this.unit = unit;
    }

    public TypeCategory(String id, String label, Unit unit) {
        super(id, label);
        this.unit = unit;
    }

    public TypeCategory(String id, String label, Category parent, Unit unit) {
        super(id, label, parent);
        this.unit = unit;
    }
}
