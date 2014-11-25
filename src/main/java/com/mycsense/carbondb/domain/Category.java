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

import java.util.ArrayList;

public class Category
{
    protected String id;
    protected String label;
    protected ArrayList<Object> children = new ArrayList<>();
    protected Category parent;

    public Category() {
        id = "";
    }

    public Category(String id) {
        this.id = id;
    }

    public Category(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public Category(String id, String label, Category parent) {
        this.id = id;
        this.label = label;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public void setURI(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public ArrayList<Object> getChildren() {
        return children;
    }

    public void addChild(Object child) {
        children.add(child);
    }
}