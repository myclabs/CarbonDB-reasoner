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

        dim1.add(kw2);
        dim2.add(kw2);
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

        dim2.add(kw1);
        assertFalse(dim1.equals(dim2));
    }
}
