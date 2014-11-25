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

import com.mycsense.carbondb.domain.elementaryFlow.DataSource;

public class ElementaryFlow {
    protected ElementaryFlowType type;
    protected Value value;
    protected DataSource dataSource;

    public ElementaryFlow(ElementaryFlowType type, Value value) {
        this(type, value, DataSource.INPUT);
    }

    public ElementaryFlow(ElementaryFlowType type, Value value, DataSource dataSource) {
        this.type = type;
        this.value = value;
        this.dataSource = dataSource;
    }

    public ElementaryFlowType getType() {
        return type;
    }

    public void setType(ElementaryFlowType type) {
        this.type = type;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
