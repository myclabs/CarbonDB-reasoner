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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.mycsense.carbondb.domain.Dimension;
import com.mycsense.carbondb.domain.DimensionSet;
import com.mycsense.carbondb.domain.Keyword;
import org.junit.Test;
import org.junit.Before;

/**
 * Unit test for DimensionSet.
 */
public class DimensionSetTest 
{
    Keyword kw1, kw2, kw3, kw4;
    Dimension dim12, dim34;

    @Before public void setUp() {
        kw1 = new Keyword("kw1");
        kw2 = new Keyword("kw2");
        kw3 = new Keyword("kw3");
        kw4 = new Keyword("kw4");

        dim12 = new Dimension(kw1, kw2);
        dim34 = new Dimension(kw3, kw4);
    }

    /**
    * Test for DimensionSet.equals
    */
    @Test public void equalsWithEmptyDimensionSetB()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12, dim34);
        DimensionSet dimSet2 = new DimensionSet();

        assertFalse(dimSet1.equals(dimSet2));
    }

    /**
    * Test for DimensionSet.equals
    */
    @Test public void equalsWithSameDimensionSet()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12, dim34);

        assertTrue(dimSet1.equals(dimSet1));
    }

    /**
    * Test for DimensionSet.equals
    */
    @Test public void equalsWithEquivalentDimensionSets()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12, dim34);
        Dimension dim212 = new Dimension(kw1, kw2);
        Dimension dim234 = new Dimension(kw3, kw4);
        DimensionSet dimSet2 = new DimensionSet(dim212, dim234);

        assertTrue(dimSet1.equals(dimSet2));
    }

    /**
    * Test for DimensionSet.equals
    */
    @Test public void equalsWithDifferentDimensionSets()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12, dim34);

        DimensionSet dimSet2 = new DimensionSet();
        Dimension dim1 = new Dimension(kw1);
        dimSet2.add(dim1);

        assertFalse(dimSet1.equals(dimSet2));

        Dimension dim234 = new Dimension(kw3);
        dimSet2.add(dim234);
        assertFalse(dimSet1.equals(dimSet2));

        dim234.addKeyword(kw4);
        assertFalse(dimSet1.equals(dimSet2));
    }

    /**
     * Test for DimensionSet.combinations
     */
    @Test public void getCombinationsWithEmptySet()
    {
        DimensionSet dimSet = new DimensionSet();
        assertEquals(0, dimSet.combinations().size());
    }

    /**
     * Test for DimensionSet.combinations
     */
    @Test public void getCombinationsWithOneDimensionDimSet()
    {
        DimensionSet dimSet = new DimensionSet(dim12);

        DimensionSet dimSetResult = dimSet.combinations();

        Dimension expectedDim1 = new Dimension(kw1);
        Dimension expectedDim2 = new Dimension(kw2);
        DimensionSet expectedDimSet = new DimensionSet(
            expectedDim1, expectedDim2);

        assertEquals(2, dimSetResult.size());
        assertTrue(dimSetResult.equals(expectedDimSet));
    }

    /**
     * Test for DimensionSet.combinations
     */
    @Test public void getCombinations()
    {
        DimensionSet dimSet = new DimensionSet(dim12, dim34);

        DimensionSet dimSetResult = dimSet.combinations();

        Dimension expectedDim1 = new Dimension(kw1, kw3);
        Dimension expectedDim2 = new Dimension(kw1, kw4);
        Dimension expectedDim3 = new Dimension(kw2, kw3);
        Dimension expectedDim4 = new Dimension(kw2, kw4);
        DimensionSet expectedDimSet = new DimensionSet(
            expectedDim1, expectedDim2, expectedDim3, expectedDim4);

        assertEquals(4, dimSetResult.size());
        assertTrue(dimSetResult.equals(expectedDimSet));
    }

    /**
     * Test for DimensionSet.commonKeywords
     */
    @Test public void getCommonKeywordsWithEmptyDimSet()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12);
        DimensionSet dimSet2 = new DimensionSet();

        assertEquals(0, dimSet1.commonKeywords(dimSet2).size());
    }

    /**
     * Test for DimensionSet.commonKeywords
     */
    @Test public void getCommonKeywordsWithDifferentDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12);
        DimensionSet dimSet2 = new DimensionSet(dim34);

        assertEquals(0, dimSet1.commonKeywords(dimSet2).size());
    }

    /**
     * Test for DimensionSet.commonKeywords
     */
    @Test public void getCommonKeywordsWithOverlappingDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet();
        dimSet1.add(dim12);

        DimensionSet dimSet2 = new DimensionSet();
        Dimension dim1 = new Dimension(kw1, kw3);
        Dimension dim2 = new Dimension(kw2, kw4);
        dimSet2.add(dim1);
        dimSet2.add(dim2);

        Dimension dimResult = dimSet1.commonKeywords(dimSet2);
        Dimension expectedDim = new Dimension(kw1, kw2);

        assertEquals(2, dimResult.size());
        assertTrue(expectedDim.equals(dimResult));
    }

    @Test public void testForbiddenActionWithHashCode()
    {
        DimensionSet dimSet = new DimensionSet();
        dimSet.add(dim12);
        assertTrue(dimSet.contains(dim12));
        dim12.addKeyword(kw3);
        // The hashcode for the dimension has changed,
        // contains uses this hashcode
        // thus we cannot find our dimension in the dimSet
        assertFalse(dimSet.contains(dim12));
    }

    /**
     * Test for DimensionSet.alpha
     */
    @Test public void alphaWithEmptyDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet();
        DimensionSet dimSet2 = new DimensionSet();

        assertTrue(dimSet1.alpha(dimSet2).equals(0));
    }

    /**
     * Test for DimensionSet.alpha
     */
    @Test public void alphaWithSameDimSet()
    {
        DimensionSet dimSet = new DimensionSet(dim12);

        assertTrue(dimSet.alpha(dimSet).equals(1));
    }

    /**
     * Test for DimensionSet.alpha
     */
    @Test public void alphaWithOverlapppingDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12, dim34);
        Keyword kw5 = new Keyword("kw5");
        Keyword kw6 = new Keyword("kw6");
        Keyword kw7 = new Keyword("kw7");
        Keyword kw8 = new Keyword("kw8");
        DimensionSet dimSet2 = new DimensionSet(new Dimension(kw1, kw5),
                                                new Dimension(kw3, kw6),
                                                new Dimension(kw7, kw8));

        assertTrue(dimSet1.alpha(dimSet2).equals(2));
    }

    /**
     * Test for DimensionSet.alpha
     */
    @Test
    public void alphaWithDifferentDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12);
        DimensionSet dimSet2 = new DimensionSet(dim34);

        assertTrue(dimSet1.alpha(dimSet2).equals(0));
    }

    /**
     * Test for DimensionSet.union
     */
    @Test public void unionWithEmptyDimSets()
    {
        // empty, same, overlapping, different
        DimensionSet dimSet = new DimensionSet();
        DimensionSet.UnionResult ur = dimSet.union(dimSet);

        assertTrue(ur.alpha.equals(0));
        assertEquals(0, ur.commonKeywords.size());
        assertEquals(0, ur.dimSet.size());
    }

    /**
     * Test for DimensionSet.union
     */
    @Test public void unionWithSameDimSets()
    {
        DimensionSet dimSet = new DimensionSet(dim12, dim34);
        DimensionSet.UnionResult ur = dimSet.union(dimSet);

        assertTrue(ur.alpha.equals(2));

        assertEquals(4, ur.commonKeywords.size());
        Dimension expectedCommonKeywords = dimSet.commonKeywords(dimSet);
        assertTrue(ur.commonKeywords.equals(expectedCommonKeywords));

        assertEquals(2, ur.dimSet.size());
        assertTrue(ur.dimSet.equals(dimSet));
    }

    /**
     * Test for DimensionSet.union
     */
    @Test public void unionWithOverlappingDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12, dim34);
        Keyword kw5 = new Keyword("kw5");
        Keyword kw6 = new Keyword("kw6");
        Keyword kw7 = new Keyword("kw7");
        Keyword kw8 = new Keyword("kw8");
        DimensionSet dimSet2 = new DimensionSet(new Dimension(kw1, kw5),
                                                new Dimension(kw3, kw6),
                                                new Dimension(kw7, kw8));

        DimensionSet.UnionResult ur = dimSet1.union(dimSet2);

        assertTrue(ur.alpha.equals(2));

        assertEquals(2, ur.commonKeywords.size());
        Dimension expectedCommonKeywords = dimSet1.commonKeywords(dimSet2);
        assertTrue(ur.commonKeywords.equals(expectedCommonKeywords));

        assertEquals(3, ur.dimSet.size());

        DimensionSet expectedDimSet = new DimensionSet(
            new Dimension(kw1),
            new Dimension(kw3),
            new Dimension(kw7, kw8)
        );
        assertTrue(ur.dimSet.equals(expectedDimSet));
    }

    /**
     * Test for DimensionSet.union
     */
    @Test public void unionWithDifferentDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12);
        DimensionSet dimSet2 = new DimensionSet(dim34);

        DimensionSet.UnionResult ur = dimSet1.union(dimSet2);

        assertTrue(ur.alpha.equals(0));

        assertEquals(0, ur.commonKeywords.size());
        Dimension expectedCommonKeywords = dimSet1.commonKeywords(dimSet2);
        assertTrue(ur.commonKeywords.equals(expectedCommonKeywords));

        assertEquals(2, ur.dimSet.size());

        DimensionSet expectedDimSet = new DimensionSet(
            new Dimension(kw1,kw2),
            new Dimension(kw3,kw4)
        );
        assertTrue(ur.dimSet.equals(expectedDimSet));
    }

    /**
     * Test for DimensionSet.numberOfIntersections
     */
    @Test public void numberOfIntersections()
    {
        DimensionSet dimSet = new DimensionSet(dim12);

        assertEquals(0, dimSet.numberOfIntersections(dim34));
        assertEquals(1, dimSet.numberOfIntersections(dim12));
    }

    /**
     * Test for DimensionSet.isCompatible
     */
    @Test public void isCompatibleWithDisjointDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12);
        DimensionSet dimSet2 = new DimensionSet(dim34);

        assertTrue(dimSet1.isCompatible(dimSet2));
        assertTrue(dimSet2.isCompatible(dimSet1));
    }

    /**
     * Test for DimensionSet.isCompatible
     */
    @Test public void isCompatibleWithOverlappingDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12, dim34);
        DimensionSet dimSet2 = new DimensionSet(dim12);

        assertTrue(dimSet1.isCompatible(dimSet2));
        assertTrue(dimSet2.isCompatible(dimSet1));
    }

    /**
     * Test for DimensionSet.isCompatible
     */
    @Test public void isCompatibleWithIncompatibleDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet(dim12, dim34);
        Dimension dim = new Dimension(kw1, kw3);
        DimensionSet dimSet2 = new DimensionSet(dim12, dim);

        assertFalse(dimSet1.isCompatible(dimSet2));
        assertFalse(dimSet2.isCompatible(dimSet1));
    }
}
