package com.mycsense.carbondb;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.mycsense.carbondb.architecture.Datatype;
import com.mycsense.carbondb.architecture.UnitsRepo;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.relation.Type;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * Unit test for SourceRelation.
 */
public class SourceRelationTest
{
    Keyword kw1, kw2, kw3, kw4;
    Dimension dim12, dim34;
    Unit unit;
    UnitsRepo unitsRepo;
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

        unitsRepo = Mockito.mock(UnitsRepo.class);
        Mockito.when(unitsRepo.getConversionFactor(Mockito.anyString())).thenReturn(1.0);
        Mockito.when(unitsRepo.areCompatible(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(unitsRepo.areCompatible(Mockito.any(Unit.class), Mockito.anyString())).thenReturn(true);
        Mockito.when(unitsRepo.areCompatible(Mockito.any(Unit.class), Mockito.any(Unit.class))).thenReturn(true);
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
     */
    @Test public void translate()
    {
        // empty OK, same (i.e. : normal) OK, overlapping (i.e. : projection and aggregation) OK, different TODO
        // normal OK, partial OK, projection OK, aggregation OK
    }

    /**
     * Test for SourceRelation.translate
     */
    @Test public void translateWithEmptyGroups()
    {
        DimensionSet dimSet = new DimensionSet();
        Group upstreamGroup = new Group(dimSet);
        Group coeffGroup = new Group(dimSet);
        Group downstreamGroup = new Group(dimSet);
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup, unitsRepo);

        ArrayList<DerivedRelation> derivedRelations = new ArrayList<>();
        try {
            derivedRelations = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        assertEquals(0, derivedRelations.size());
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
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup, unitsRepo);

        ArrayList<DerivedRelation> derivedRelations = new ArrayList<>();
        try {
            derivedRelations = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<DerivedRelation> expectedDerivedRelations = new ArrayList<>();
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim14 = new Dimension(kw1, kw4);
        Dimension dim23 = new Dimension(kw2, kw3);
        Dimension dim24 = new Dimension(kw2, kw4);
        expectedDerivedRelations.add(new DerivedRelation(dim13, unit, dim13, unit, dim13, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim14, unit, dim14, unit, dim14, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim23, unit, dim23, unit, dim23, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim24, unit, dim24, unit, dim24, unit));
        assertEquals(4, derivedRelations.size());
        assertTrue(derivedRelations.containsAll(expectedDerivedRelations));
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
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup, unitsRepo);

        ArrayList<DerivedRelation> derivedRelations = new ArrayList<>();
        try {
            derivedRelations = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<DerivedRelation> expectedDerivedRelations = new ArrayList<>();
        Dimension dim1 = new Dimension(kw1);
        Dimension dim2 = new Dimension(kw2);
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim14 = new Dimension(kw1, kw4);
        Dimension dim23 = new Dimension(kw2, kw3);
        Dimension dim24 = new Dimension(kw2, kw4);
        expectedDerivedRelations.add(new DerivedRelation(dim13, unit, dim13, unit, dim1, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim14, unit, dim14, unit, dim1, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim23, unit, dim23, unit, dim2, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim24, unit, dim24, unit, dim2, unit));
        assertEquals(4, derivedRelations.size());
        assertTrue(derivedRelations.containsAll(expectedDerivedRelations));
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
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup, unitsRepo);

        ArrayList<DerivedRelation> derivedRelations = new ArrayList<>();
        try {
            derivedRelations = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<DerivedRelation> expectedDerivedRelations = new ArrayList<>();
        Dimension dim1 = new Dimension(kw1);
        Dimension dim2 = new Dimension(kw2);
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim14 = new Dimension(kw1, kw4);
        Dimension dim23 = new Dimension(kw2, kw3);
        Dimension dim24 = new Dimension(kw2, kw4);
        expectedDerivedRelations.add(new DerivedRelation(dim1, unit, dim1, unit, dim13, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim1, unit, dim1, unit, dim14, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim2, unit, dim2, unit, dim23, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim2, unit, dim2, unit, dim24, unit));
        assertEquals(4, derivedRelations.size());
        assertTrue(derivedRelations.containsAll(expectedDerivedRelations));
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
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup, unitsRepo);

        ArrayList<DerivedRelation> derivedRelations = new ArrayList<>();
        try {
            derivedRelations = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<DerivedRelation> expectedDerivedRelations = new ArrayList<>();
        Dimension dim13 = new Dimension(kw1, kw3);
        Dimension dim23 = new Dimension(kw2, kw3);
        expectedDerivedRelations.add(new DerivedRelation(dim13, unit, dim13, unit, dim13, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim23, unit, dim23, unit, dim23, unit));
        assertEquals(2, derivedRelations.size());
        assertTrue(derivedRelations.containsAll(expectedDerivedRelations));
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
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup, unitsRepo);

        ArrayList<DerivedRelation> derivedRelations = new ArrayList<>();
        try {
            derivedRelations = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<DerivedRelation> expectedDerivedRelations = new ArrayList<>();
        Dimension dim1 = new Dimension(kw1);
        Dimension dim2 = new Dimension(kw2);
        Dimension dim3 = new Dimension(kw3);
        Dimension dim4 = new Dimension(kw4);
        expectedDerivedRelations.add(new DerivedRelation(dim1, unit, dim1, unit, dim3, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim1, unit, dim1, unit, dim4, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim2, unit, dim2, unit, dim3, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim2, unit, dim2, unit, dim4, unit));
        assertEquals(4, derivedRelations.size());
        assertTrue(derivedRelations.containsAll(expectedDerivedRelations));
    }

    /**
     * Test for SourceRelation.translate
     */
    @Test public void translateNormalSourceRelationWithCommonKeywords()
    {
        DimensionSet dimSet = new DimensionSet(dim12, dim34);
        Keyword commonKeyword = new Keyword("commonKw");
        Dimension commonKeywords = new Dimension(commonKeyword);

        Group upstreamGroup = new Group(dimSet, commonKeywords);
        Group coeffGroup = new Group(dimSet, commonKeywords);
        Group downstreamGroup = new Group(dimSet, commonKeywords);
        upstreamGroup.setUnit(unit);
        coeffGroup.setUnit(unit);
        downstreamGroup.setUnit(unit);
        SourceRelation sourceRelation = new SourceRelation(upstreamGroup, coeffGroup, downstreamGroup, unitsRepo);

        ArrayList<DerivedRelation> derivedRelations = new ArrayList<>();
        try {
            derivedRelations = sourceRelation.translate();
        } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
            e.printStackTrace();
        }

        ArrayList<DerivedRelation> expectedDerivedRelations = new ArrayList<>();
        Dimension dim13c = new Dimension(kw1, kw3, commonKeyword);
        Dimension dim14c = new Dimension(kw1, kw4, commonKeyword);
        Dimension dim23c = new Dimension(kw2, kw3, commonKeyword);
        Dimension dim24c = new Dimension(kw2, kw4, commonKeyword);
        expectedDerivedRelations.add(new DerivedRelation(dim13c, unit, dim13c, unit, dim13c, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim14c, unit, dim14c, unit, dim14c, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim23c, unit, dim23c, unit, dim23c, unit));
        expectedDerivedRelations.add(new DerivedRelation(dim24c, unit, dim24c, unit, dim24c, unit));
        assertEquals(4, derivedRelations.size());
        assertTrue(derivedRelations.containsAll(expectedDerivedRelations));
    }
}
