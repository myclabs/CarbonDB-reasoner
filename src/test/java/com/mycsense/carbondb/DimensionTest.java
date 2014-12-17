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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.mycsense.carbondb.domain.Dimension;
import com.mycsense.carbondb.domain.Keyword;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit test for Dimension.
 */
@RunWith(JUnit4.class)
public class DimensionTest 
{
    Keyword kw1, kw2, kw3, kw4;

    @Before
    public void setUp() {
        kw1 = new Keyword("kw1");
        kw2 = new Keyword("kw2");
        kw3 = new Keyword("kw3");
        kw4 = new Keyword("kw4");
    }

    /**
     * Test for Dimension.hasCommonKeywords
     */
    @Test
    public void hasCommonKeywordsWithEmptyDimSets()
    {
        Dimension dim1 = new Dimension();
        Dimension dim2 = new Dimension();

        assertFalse(dim1.hasCommonKeywords(dim2));
    }

    /**
     * Test for Dimension.hasCommonKeywords
     */
    @Test
    public void hasCommonKeywordsWithEmptyDimSetB()
    {
        Dimension dim1 = new Dimension(kw1);
        Dimension dim2 = new Dimension();

        assertFalse(dim1.hasCommonKeywords(dim2));
    }

    /**
     * Test for Dimension.hasCommonKeywords
     */
    @Test
    public void hasNoCommonKeywords()
    {
        Dimension dim1 = new Dimension(kw1, kw2, kw4);
        Dimension dim2 = new Dimension(kw3);

        assertFalse(dim1.hasCommonKeywords(dim2));
    }
    /**
     * Test for Dimension.hasCommonKeywords
     */
    @Test
    public void hasOneCommonKeywords()
    {
        Dimension dim1 = new Dimension(kw1, kw2, kw4);
        Dimension dim2 = new Dimension(kw3, kw4);

        assertTrue(dim1.hasCommonKeywords(dim2));
    }

    /**
    * Test for Dimension.equals
    */
    @Test public void equalsWithSameDim()
    {
        Dimension dim = new Dimension(kw1);

        assertTrue(dim.equals(dim));
    }

    /**
    * Test for Dimension.equals
    */
    @Test public void equalsWithEquivalentDims()
    {
        Dimension dim1 = new Dimension(kw1);
        Dimension dim2 = new Dimension(kw1);

        assertTrue(dim1.equals(dim2));

        dim1.addKeyword(kw2);
        dim2.addKeyword(kw2);
        assertTrue(dim1.equals(dim2));
    }

    /**
    * Test for Dimension.equals
    */
    @Test public void equalsWithDifferentDims()
    {
        Dimension dim1 = new Dimension(kw1, kw2);
        Dimension dim2 = new Dimension(kw3);

        assertFalse(dim1.equals(dim2));

        dim2.addKeyword(kw1);
        assertFalse(dim1.equals(dim2));
    }
}
