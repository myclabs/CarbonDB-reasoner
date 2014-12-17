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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.mycsense.carbondb.architecture.Datatype;
import com.mycsense.carbondb.domain.Dimension;
import com.mycsense.carbondb.domain.DimensionSet;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.Keyword;
import com.mycsense.carbondb.domain.RelationType;
import com.mycsense.carbondb.domain.SourceRelation;
import com.mycsense.carbondb.domain.Unit;
import com.mycsense.carbondb.domain.UnitTools;
import com.mycsense.carbondb.domain.relation.TranslationDerivative;
import com.mycsense.carbondb.domain.relation.Type;

import org.junit.Test;
import org.junit.Before;

import org.mockito.Mockito;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Unit test for SourceRelation.
 */
public class SourceRelationTest
{
    Keyword kw1, kw2, kw3, kw4;
    Dimension dim12, dim34;
    Unit unit;
    UnitTools unitTools;
    RelationType type;

    @Before
    public void setUp() {
        kw1 = new Keyword("kw1");
        kw2 = new Keyword("kw2");
        kw3 = new Keyword("kw3");
        kw4 = new Keyword("kw4");

        dim12 = new Dimension(kw1, kw2);
        dim34 = new Dimension(kw3, kw4);

        unit = new Unit(null, "", "");

        type = new RelationType(Datatype.getURI() + "type", "type", Type.SYNCHRONOUS);

        unitTools = Mockito.mock(UnitTools.class);
        Mockito.when(unitTools.getConversionFactor(Mockito.any(Unit.class))).thenReturn(1.0);
        Mockito.when(unitTools.areCompatible(Mockito.any(Unit.class), Mockito.any(Unit.class))).thenReturn(true);
        Unit.setUnitTools(unitTools);
    }

    /**
     * Test for SourceRelation.getHashKey
     */
    @Test public void getHashKeyWithZeroAlpha()
    {
        assertTrue(SourceRelation.getHashKey(new Dimension(), new Dimension(), 0).equals("#emptyHashKey#"));
    }

    /**
     * Test for SourceRelation.getHashKey
     */
    @Test public void getHashKeyWithDifferentDimensions()
    {
        assertTrue(SourceRelation.getHashKey(dim12, dim34, 1).equals("#nullHashKey#"));
    }

    /**
     * Test for SourceRelation.getHashKey
     */
    @Test public void getHashKeyWithOverlappingDimensionsAndAppropriateAlpha()
    {
        Dimension commonKeywords = new Dimension(kw4, kw1, kw3, kw2);
        assertTrue(SourceRelation.getHashKey(dim12, commonKeywords, 2).equals("kw1,kw2"));
    }

    /**
     * Test for SourceRelation.getHashKey
     */
    @Test public void getHashKeyWithOverlappingDimensionsAndUnappropriateAlpha()
    {
        Dimension commonKeywords = new Dimension(kw4, kw1, kw3, kw2);
        assertTrue(SourceRelation.getHashKey(dim12, commonKeywords, 1).equals("#nullHashKey#"));
    }

    /**
     * Test for SourceRelation.createGroupHashTable
     */
    @Test public void createGroupHashTable()
    {
        Group group = new Group(new DimensionSet(dim12, dim34));
        HashMap<String, ArrayList<Dimension>> groupHashTable;
        groupHashTable = SourceRelation.createGroupHashTable(group, new Dimension(kw1, kw2, kw3, kw4), 2);

        assertEquals(4, groupHashTable.size());
        assertEquals(1, groupHashTable.get("kw1,kw3").size());
        assertEquals(1, groupHashTable.get("kw1,kw4").size());
        assertEquals(1, groupHashTable.get("kw2,kw3").size());
        assertEquals(1, groupHashTable.get("kw2,kw4").size());

        Dimension expectedDimension13 = new Dimension(kw1,kw3);
        assertTrue(groupHashTable.get("kw1,kw3").get(0).equals(expectedDimension13));
        Dimension expectedDimension14 = new Dimension(kw1,kw4);
        assertTrue(groupHashTable.get("kw1,kw4").get(0).equals(expectedDimension14));
        Dimension expectedDimension23 = new Dimension(kw2,kw3);
        assertTrue(groupHashTable.get("kw2,kw3").get(0).equals(expectedDimension23));
        Dimension expectedDimension24 = new Dimension(kw2,kw4);
        assertTrue(groupHashTable.get("kw2,kw4").get(0).equals(expectedDimension24));
    }

    /**
     * Test for SourceRelation.createGroupHashTable
     */
    @Test public void createGroupHashTableWithUnselectedElements()
    {
        Group group = new Group(new DimensionSet(dim12, dim34));
        HashMap<String, ArrayList<Dimension>> groupHashTable;
        groupHashTable = SourceRelation.createGroupHashTable(group, new Dimension(kw1, kw3, kw4), 2);

        assertEquals(2, groupHashTable.size());
        assertEquals(1, groupHashTable.get("kw1,kw3").size());
        assertEquals(1, groupHashTable.get("kw1,kw4").size());

        Dimension expectedDimension13 = new Dimension(kw1,kw3);
        assertTrue(groupHashTable.get("kw1,kw3").get(0).equals(expectedDimension13));
        Dimension expectedDimension14 = new Dimension(kw1,kw4);
        assertTrue(groupHashTable.get("kw1,kw4").get(0).equals(expectedDimension14));
    }

    /**
     * Test for SourceRelation.translate
     * The tests should contains the following cases :
     *  empty, same (i.e. : normal), overlapping (i.e. : projection and aggregation), different TODO
     *  normal, partial, projection, aggregation
     */
    @Test public void translateWithEmptyGroups()
    {
        DimensionSet dimSet = new DimensionSet();
        Group upstreamGroup = new Group(dimSet);
        Group coeffGroup = new Group(dimSet);
        Group downstreamGroup = new Group(dimSet);
        upstreamGroup.setUnit(unit);
        coeffGroup.setUnit(unit);
        downstreamGroup.setUnit(unit);
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<TranslationDerivative> translationDerivatives = new ArrayList<>();
        try {
            translationDerivatives = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        assertEquals(0, translationDerivatives.size());
    }

    /**
     * Test for SourceRelation.translate
     */
    @Test public void translateNormalSourceRelation()
    {
        DimensionSet dimSet = new DimensionSet(dim12, dim34);
        Group upstreamGroup = new Group(dimSet);
        Group coeffGroup = new Group(dimSet);
        Group downstreamGroup = new Group(dimSet);
        upstreamGroup.setUnit(unit);
        coeffGroup.setUnit(unit);
        downstreamGroup.setUnit(unit);
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<TranslationDerivative> derivatives = new ArrayList<>();
        try {
            derivatives = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<TranslationDerivative> expected = new ArrayList<>();
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim14 = new Dimension(kw1, kw4);
        Dimension dim23 = new Dimension(kw2, kw3);
        Dimension dim24 = new Dimension(kw2, kw4);
        expected.add(new TranslationDerivative(dim13, dim13, dim13, sourceRelation));
        expected.add(new TranslationDerivative(dim14, dim14, dim14, sourceRelation));
        expected.add(new TranslationDerivative(dim23, dim23, dim23, sourceRelation));
        expected.add(new TranslationDerivative(dim24, dim24, dim24, sourceRelation));
        assertEquals(4, derivatives.size());
        assertTrue(derivatives.containsAll(expected));
    }

    /**
     * Test for SourceRelation.translate
     */
    @Test public void translateAggregationSourceRelation()
    {
        DimensionSet dimSet1234 = new DimensionSet(dim12, dim34);
        DimensionSet dimSet12 = new DimensionSet(dim12);
        Group upstreamGroup = new Group(dimSet1234);
        Group coeffGroup = new Group(dimSet1234);
        Group downstreamGroup = new Group(dimSet12);
        upstreamGroup.setUnit(unit);
        coeffGroup.setUnit(unit);
        downstreamGroup.setUnit(unit);
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<TranslationDerivative> derivatives = new ArrayList<>();
        try {
            derivatives = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<TranslationDerivative> expected = new ArrayList<>();
        Dimension dim1 = new Dimension(kw1);
        Dimension dim2 = new Dimension(kw2);
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim14 = new Dimension(kw1, kw4);
        Dimension dim23 = new Dimension(kw2, kw3);
        Dimension dim24 = new Dimension(kw2, kw4);
        expected.add(new TranslationDerivative(dim13, dim13, dim1, sourceRelation));
        expected.add(new TranslationDerivative(dim14, dim14, dim1, sourceRelation));
        expected.add(new TranslationDerivative(dim23, dim23, dim2, sourceRelation));
        expected.add(new TranslationDerivative(dim24, dim24, dim2, sourceRelation));
        assertEquals(4, derivatives.size());
        assertTrue(derivatives.containsAll(expected));
    }

    /**
     * Test for SourceRelation.translate
     */
    @Test public void translateProjectionSourceRelation()
    {
        DimensionSet dimSet1234 = new DimensionSet(dim12, dim34);
        DimensionSet dimSet12 = new DimensionSet(dim12);
        Group upstreamGroup = new Group(dimSet12);
        Group coeffGroup = new Group(dimSet12);
        Group downstreamGroup = new Group(dimSet1234);
        upstreamGroup.setUnit(unit);
        coeffGroup.setUnit(unit);
        downstreamGroup.setUnit(unit);
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<TranslationDerivative> derivatives = new ArrayList<>();
        try {
            derivatives = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<TranslationDerivative> expected = new ArrayList<>();
        Dimension dim1 = new Dimension(kw1);
        Dimension dim2 = new Dimension(kw2);
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim14 = new Dimension(kw1, kw4);
        Dimension dim23 = new Dimension(kw2, kw3);
        Dimension dim24 = new Dimension(kw2, kw4);
        expected.add(new TranslationDerivative(dim1, dim1, dim13, sourceRelation));
        expected.add(new TranslationDerivative(dim1, dim1, dim14, sourceRelation));
        expected.add(new TranslationDerivative(dim2, dim2, dim23, sourceRelation));
        expected.add(new TranslationDerivative(dim2, dim2, dim24, sourceRelation));
        assertEquals(4, derivatives.size());
        assertTrue(derivatives.containsAll(expected));
    }

    /**
     * Test for SourceRelation.translate
     */
    @Test public void translatePartialSourceRelation()
    {
        DimensionSet dimSet1234 = new DimensionSet(dim12, dim34);
        DimensionSet dimSet123 = new DimensionSet(dim12, new Dimension(kw3));
        Group upstreamGroup = new Group(dimSet1234);
        Group coeffGroup = new Group(dimSet1234);
        Group downstreamGroup = new Group(dimSet123);
        upstreamGroup.setUnit(unit);
        coeffGroup.setUnit(unit);
        downstreamGroup.setUnit(unit);
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<TranslationDerivative> derivatives = new ArrayList<>();
        try {
            derivatives = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<TranslationDerivative> expected = new ArrayList<>();
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim23 = new Dimension(kw2, kw3);
        expected.add(new TranslationDerivative(dim13, dim13, dim13, sourceRelation));
        expected.add(new TranslationDerivative(dim23, dim23, dim23, sourceRelation));
        assertEquals(2, derivatives.size());
        assertTrue(derivatives.containsAll(expected));
    }

    /**
     * Test for SourceRelation.translate
     */
    @Test public void translateWithDifferentGroups()
    {
        DimensionSet dimSet12 = new DimensionSet(dim12);
        DimensionSet dimSet34 = new DimensionSet(dim34);
        Group upstreamGroup = new Group(dimSet12);
        Group coeffGroup = new Group(dimSet12);
        Group downstreamGroup = new Group(dimSet34);
        upstreamGroup.setUnit(unit);
        coeffGroup.setUnit(unit);
        downstreamGroup.setUnit(unit);
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<TranslationDerivative> derivatives = new ArrayList<>();
        try {
            derivatives = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<TranslationDerivative> expected = new ArrayList<>();
        Dimension dim1 = new Dimension(kw1);
        Dimension dim2 = new Dimension(kw2);
        Dimension dim3 = new Dimension(kw3);
        Dimension dim4 = new Dimension(kw4);
        expected.add(new TranslationDerivative(dim1, dim1, dim3, sourceRelation));
        expected.add(new TranslationDerivative(dim1, dim1, dim4, sourceRelation));
        expected.add(new TranslationDerivative(dim2, dim2, dim3, sourceRelation));
        expected.add(new TranslationDerivative(dim2, dim2, dim4, sourceRelation));
        assertEquals(4, derivatives.size());
        assertTrue(derivatives.containsAll(expected));
    }

    /**
     * Test for SourceRelation.translate
     */
    @Test public void translateNormalSourceRelationWithCommonKeywords()
    {
        DimensionSet dimSet = new DimensionSet(dim12, dim34);
        Keyword commonKeyword = new Keyword("commonKw");
        TreeSet<Keyword> commonKeywords = new TreeSet<>();
        commonKeywords.add(commonKeyword);

        Group upstreamGroup = new Group(dimSet, commonKeywords);
        Group coeffGroup = new Group(dimSet, commonKeywords);
        Group downstreamGroup = new Group(dimSet, commonKeywords);
        upstreamGroup.setUnit(unit);
        coeffGroup.setUnit(unit);
        downstreamGroup.setUnit(unit);
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<TranslationDerivative> derivatives = new ArrayList<>();
        try {
            derivatives = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<TranslationDerivative> expected = new ArrayList<>();
        Dimension dim13c = new Dimension(kw1, kw3, commonKeyword);
        Dimension dim14c = new Dimension(kw1, kw4, commonKeyword);
        Dimension dim23c = new Dimension(kw2, kw3, commonKeyword);
        Dimension dim24c = new Dimension(kw2, kw4, commonKeyword);
        expected.add(new TranslationDerivative(dim13c, dim13c, dim13c, sourceRelation));
        expected.add(new TranslationDerivative(dim14c, dim14c, dim14c, sourceRelation));
        expected.add(new TranslationDerivative(dim23c, dim23c, dim23c, sourceRelation));
        expected.add(new TranslationDerivative(dim24c, dim24c, dim24c, sourceRelation));
        assertEquals(4, derivatives.size());
        assertTrue(derivatives.containsAll(expected));
    }
}
