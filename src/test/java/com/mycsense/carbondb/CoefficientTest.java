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

package com.mycsense.carbondb;

import static org.junit.Assert.assertEquals;

import com.mycsense.carbondb.domain.Coefficient;
import com.mycsense.carbondb.domain.Dimension;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.Keyword;
import com.mycsense.carbondb.domain.Unit;
import com.mycsense.carbondb.domain.UnitTools;
import com.mycsense.carbondb.domain.Value;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/**
 * Unit test for Coefficient (and SingleElement).
 */
@RunWith(JUnit4.class)
public class CoefficientTest {
    Keyword kw1, kw2, kw3, kw4;
    Dimension dim12, dim34;
    Unit unit;
    UnitTools unitTools;

    @Before
    public void setUp() {
        kw1 = new Keyword("kw1");
        kw2 = new Keyword("kw2");
        kw3 = new Keyword("kw3");
        kw4 = new Keyword("kw4");

        dim12 = new Dimension(kw1, kw2);
        dim34 = new Dimension(kw3, kw4);

        unit = new Unit("unit", "", "");

        unitTools = Mockito.mock(UnitTools.class);
        Mockito.when(unitTools.getConversionFactor(Mockito.any(Unit.class))).thenReturn(1.0);
        Mockito.when(unitTools.areCompatible(Mockito.any(Unit.class), Mockito.any(Unit.class))).thenReturn(true);
        Unit.setUnitTools(unitTools);
    }

    @Test
    public void getIdWithAnId() {
        Coefficient coefficient = new Coefficient(dim12, unit, new Value(0.0, 0.0));
        coefficient.setId("test");
        assertEquals(coefficient.getId(), "test");
    }

    @Test
    public void getIdWithNoId() {
        Coefficient coefficient = new Coefficient(dim12, unit, new Value(0.0, 0.0));
        assertEquals(coefficient.getId(), "sc/kw1+kw2+unit");
    }

    @Test
    public void addGroupShouldAddOverlappingGroupToGroup() {
        Coefficient coefficient = new Coefficient(dim12, unit, new Value(0.0, 0.0));
        Group group = Mockito.mock(Group.class);
        Group otherGroup = Mockito.mock(Group.class);
        coefficient.addGroup(group);
        coefficient.addGroup(otherGroup);
        Mockito.verify(group).addOverlapingGroup(otherGroup);
        Mockito.verify(otherGroup).addOverlapingGroup(group);
    }
}
