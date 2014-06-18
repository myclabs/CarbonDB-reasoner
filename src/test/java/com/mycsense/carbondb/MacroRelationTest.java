package com.mycsense.carbondb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * Unit test for Conversion.
 */
public class MacroRelationTest 
{
    Keyword kw1, kw2, kw3, kw4;
    Dimension dim12, dim34;

    @Before
    public void setUp() {
        kw1 = new Keyword("kw1");
        kw2 = new Keyword("kw2");
        kw3 = new Keyword("kw3");
        kw4 = new Keyword("kw4");

        dim12 = new Dimension(kw1, kw2);
        dim34 = new Dimension(kw3, kw4);
    }

    /**
     * Test for Conversion.getHashKey
     */
    @Test public void getHashKeyWithZeroAlpha()
    {
        assertTrue(MacroRelation.getHashKey(new Dimension(), new Dimension(), 0).equals("#emptyHashKey#"));
    }

    /**
     * Test for Conversion.getHashKey
     */
    @Test public void getHashKeyWithDifferentDimensions()
    {
        assertTrue(MacroRelation.getHashKey(dim12, dim34, 1).equals("#nullHashKey#"));
    }

    /**
     * Test for Conversion.getHashKey
     */
    @Test public void getHashKeyWithOverlappingDimensionsAndAppropriateAlpha()
    {
        Dimension commonKeywords = new Dimension(kw4, kw1, kw3, kw2);
        assertTrue(MacroRelation.getHashKey(dim12, commonKeywords, 2).equals("kw1,kw2"));
    }

    /**
     * Test for Conversion.getHashKey
     */
    @Test public void getHashKeyWithOverlappingDimensionsAndUnappropriateAlpha()
    {
        Dimension commonKeywords = new Dimension(kw4, kw1, kw3, kw2);
        assertTrue(MacroRelation.getHashKey(dim12, commonKeywords, 1).equals("#nullHashKey#"));
    }

    /**
     * Test for Conversion.createGroupHashTable
     */
    @Test public void createGroupHashTable()
    {
        Group group = new Group(new DimensionSet(dim12, dim34));
        HashMap<String, ArrayList<Dimension>> groupHashTable;
        groupHashTable = MacroRelation.createGroupHashTable(group, new Dimension(kw1, kw2, kw3, kw4), 2);

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
     * Test for Conversion.createGroupHashTable
     */
    @Test public void createGroupHashTableWithUnselectedElements()
    {
        Group group = new Group(new DimensionSet(dim12, dim34));
        HashMap<String, ArrayList<Dimension>> groupHashTable;
        groupHashTable = MacroRelation.createGroupHashTable(group, new Dimension(kw1, kw3, kw4), 2);

        assertEquals(2, groupHashTable.size());
        assertEquals(1, groupHashTable.get("kw1,kw3").size());
        assertEquals(1, groupHashTable.get("kw1,kw4").size());

        Dimension expectedDimension13 = new Dimension(kw1,kw3);
        assertTrue(groupHashTable.get("kw1,kw3").get(0).equals(expectedDimension13));
        Dimension expectedDimension14 = new Dimension(kw1,kw4);
        assertTrue(groupHashTable.get("kw1,kw4").get(0).equals(expectedDimension14));
    }

    /**
     * Test for Conversion.translate
     */
    @Test public void translate()
    {
        // empty OK, same (i.e. : normal) OK, overlapping (i.e. : projection and aggregation) OK, different TODO
        // normal OK, partial OK, projection OK, aggregation OK
    }

    /**
     * Test for Conversion.translate
     */
    @Test public void translateWithEmptyGroups()
    {
        DimensionSet dimSet = new DimensionSet();
        Group upstreamGroup = new Group(dimSet);
        Group coeffGroup = new Group(dimSet);
        Group downstreamGroup = new Group(dimSet);
        MacroRelation macroRelation = new MacroRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<MicroRelation> microRelations;
        microRelations = macroRelation.translate();

        assertEquals(0, microRelations.size());
    }

    /**
     * Test for Conversion.translate
     */
    @Test public void translateNormalMacroRelation()
    {
        DimensionSet dimSet = new DimensionSet(dim12, dim34);
        Group upstreamGroup = new Group(dimSet);
        Group coeffGroup = new Group(dimSet);
        Group downstreamGroup = new Group(dimSet);
        MacroRelation macroRelation = new MacroRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<MicroRelation> microRelations;
        microRelations = macroRelation.translate();

        ArrayList<MicroRelation> expectedMicroRelations = new ArrayList<MicroRelation>();
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim14 = new Dimension(kw1, kw4);
        Dimension dim23 = new Dimension(kw2, kw3);
        Dimension dim24 = new Dimension(kw2, kw4);
        expectedMicroRelations.add(new MicroRelation(dim13, dim13, dim13));
        expectedMicroRelations.add(new MicroRelation(dim14, dim14, dim14));
        expectedMicroRelations.add(new MicroRelation(dim23, dim23, dim23));
        expectedMicroRelations.add(new MicroRelation(dim24, dim24, dim24));
        assertEquals(4, microRelations.size());
        assertTrue(microRelations.containsAll(expectedMicroRelations));
    }

    /**
     * Test for Conversion.translate
     */
    @Test public void translateAggregationMacroRelation()
    {
        DimensionSet dimSet1234 = new DimensionSet(dim12, dim34);
        DimensionSet dimSet12 = new DimensionSet(dim12);
        Group upstreamGroup = new Group(dimSet1234);
        Group coeffGroup = new Group(dimSet1234);
        Group downstreamGroup = new Group(dimSet12);
        MacroRelation macroRelation = new MacroRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<MicroRelation> microRelations;
        microRelations = macroRelation.translate();

        ArrayList<MicroRelation> expectedMicroRelations = new ArrayList<MicroRelation>();
        Dimension dim1 = new Dimension(kw1);
        Dimension dim2 = new Dimension(kw2);
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim14 = new Dimension(kw1, kw4);
        Dimension dim23 = new Dimension(kw2, kw3);
        Dimension dim24 = new Dimension(kw2, kw4);
        expectedMicroRelations.add(new MicroRelation(dim13, dim13, dim1));
        expectedMicroRelations.add(new MicroRelation(dim14, dim14, dim1));
        expectedMicroRelations.add(new MicroRelation(dim23, dim23, dim2));
        expectedMicroRelations.add(new MicroRelation(dim24, dim24, dim2));
        assertEquals(4, microRelations.size());
        assertTrue(microRelations.containsAll(expectedMicroRelations));
    }

    /**
     * Test for Conversion.translate
     */
    @Test public void translateProjectionMacroRelation()
    {
        DimensionSet dimSet1234 = new DimensionSet(dim12, dim34);
        DimensionSet dimSet12 = new DimensionSet(dim12);
        Group upstreamGroup = new Group(dimSet12);
        Group coeffGroup = new Group(dimSet12);
        Group downstreamGroup = new Group(dimSet1234);
        MacroRelation macroRelation = new MacroRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<MicroRelation> microRelations;
        microRelations = macroRelation.translate();

        ArrayList<MicroRelation> expectedMicroRelations = new ArrayList<MicroRelation>();
        Dimension dim1 = new Dimension(kw1);
        Dimension dim2 = new Dimension(kw2);
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim14 = new Dimension(kw1, kw4);
        Dimension dim23 = new Dimension(kw2, kw3);
        Dimension dim24 = new Dimension(kw2, kw4);
        expectedMicroRelations.add(new MicroRelation(dim1, dim1, dim13));
        expectedMicroRelations.add(new MicroRelation(dim1, dim1, dim14));
        expectedMicroRelations.add(new MicroRelation(dim2, dim2, dim23));
        expectedMicroRelations.add(new MicroRelation(dim2, dim2, dim24));
        assertEquals(4, microRelations.size());
        assertTrue(microRelations.containsAll(expectedMicroRelations));
    }

    /**
     * Test for Conversion.translate
     */
    @Test public void translatePartialMacroRelation()
    {
        DimensionSet dimSet1234 = new DimensionSet(dim12, dim34);
        DimensionSet dimSet123 = new DimensionSet(dim12, new Dimension(kw3));
        Group upstreamGroup = new Group(dimSet1234);
        Group coeffGroup = new Group(dimSet1234);
        Group downstreamGroup = new Group(dimSet123);
        MacroRelation macroRelation = new MacroRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<MicroRelation> microRelations;
        microRelations = macroRelation.translate();

        ArrayList<MicroRelation> expectedMicroRelations = new ArrayList<MicroRelation>();
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim23 = new Dimension(kw2, kw3);
        expectedMicroRelations.add(new MicroRelation(dim13, dim13, dim13));
        expectedMicroRelations.add(new MicroRelation(dim23, dim23, dim23));
        assertEquals(2, microRelations.size());
        assertTrue(microRelations.containsAll(expectedMicroRelations));
    }

    /**
     * Test for Conversion.translate
     */
    @Test public void translateWithDifferentGroups()
    {
        DimensionSet dimSet12 = new DimensionSet(dim12);
        DimensionSet dimSet34 = new DimensionSet(dim34);
        Group upstreamGroup = new Group(dimSet12);
        Group coeffGroup = new Group(dimSet12);
        Group downstreamGroup = new Group(dimSet34);
        MacroRelation macroRelation = new MacroRelation(upstreamGroup, coeffGroup, downstreamGroup);

        ArrayList<MicroRelation> microRelations;
        microRelations = macroRelation.translate();

        ArrayList<MicroRelation> expectedMicroRelations = new ArrayList<MicroRelation>();
        Dimension dim1 = new Dimension(kw1);
        Dimension dim2 = new Dimension(kw2);
        Dimension dim3 = new Dimension(kw3);
        Dimension dim4 = new Dimension(kw4);
        expectedMicroRelations.add(new MicroRelation(dim1, dim1, dim3));
        expectedMicroRelations.add(new MicroRelation(dim1, dim1, dim4));
        expectedMicroRelations.add(new MicroRelation(dim2, dim2, dim3));
        expectedMicroRelations.add(new MicroRelation(dim2, dim2, dim4));
        assertEquals(4, microRelations.size());
        assertTrue(microRelations.containsAll(expectedMicroRelations));
    }
}
