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

import java.util.HashMap;

public class ImpactType extends Type {
    protected HashMap<ElementaryFlowType, Value> components;

    public ImpactType(String id, String label, Unit unit) {
        super(id, label, unit);
        components = new HashMap<>();
    }

    public HashMap<ElementaryFlowType, Value> getComponents() {
        return components;
    }

    public void setComponents(HashMap<ElementaryFlowType, Value> components) {
        this.components = components;
    }

    public void addComponent(ElementaryFlowType flowType, Value value) {
        components.put(flowType, value);
    }
}
