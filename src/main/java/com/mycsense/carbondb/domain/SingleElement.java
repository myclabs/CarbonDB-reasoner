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

import java.util.HashSet;
import java.util.TreeSet;

public abstract class SingleElement {
    public TreeSet<Keyword> keywords;

    protected String id;
    protected Unit unit;

    protected HashSet<Group> groups;

    public SingleElement() {
        keywords = new TreeSet<>();
    }

    public SingleElement(Dimension keywords, Unit unit) {
        this.keywords = new TreeSet<>(keywords.keywords);
        this.unit = unit;
        groups = new HashSet<>();
    }

    public SingleElement(TreeSet<Keyword> keywords, Unit unit) {
        this.keywords = new TreeSet<>(keywords);
        this.unit = unit;
        groups = new HashSet<>();
    }

    public String getId() {
        if (null == id) {
            if (this instanceof Process) {
                id = "sp/";
            }
            else {
                id = "sc/";
            }
            for (Keyword keyword : keywords) {
                id += keyword.getId().replace("k/", "") + "+";
            }
            id += unit.getId().replace("u/", "");
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String toString() {
        return keywords + " ("+ unit.getSymbol() +" )";
    }

    public TreeSet<Keyword> getKeywords() {
        return keywords;
    }

    public HashSet<Group> getGroups() {
        return groups;
    }

    public void addGroup(Group group) {
        for (Group otherGroup : groups) {
            otherGroup.addOverlapingGroup(group);
            group.addOverlapingGroup(otherGroup);
        }
        groups.add(group);
    }

    @Override
    public boolean equals(Object obj) {
        // Alternative: use Guava (from Google)
        if (!(obj instanceof SingleElement))
            return false;
        if (obj == this)
            return true;

        SingleElement rhs = (SingleElement) obj;
        return new EqualsBuilder()
                .append(keywords, rhs.keywords)
                .append(unit, rhs.unit)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 67)
                .append(keywords)
                .append(unit)
                .toHashCode();
    }
}
