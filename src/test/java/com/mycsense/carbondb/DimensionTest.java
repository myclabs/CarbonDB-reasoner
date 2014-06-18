package com.mycsense.carbondb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Unit test for Dimension.
 */
@RunWith(JUnit4.class)
public class DimensionTest 
{
    Resource kw1, kw2, kw3, kw4;

    @Before
    public void setUp() {
        kw1 = ResourceFactory.createResource("kw1");
        kw2 = ResourceFactory.createResource("kw2");
        kw3 = ResourceFactory.createResource("kw3");
        kw4 = ResourceFactory.createResource("kw4");
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
        Dimension dim1 = new Dimension();
        dim1.add(kw1);
        Dimension dim2 = new Dimension();
        assertFalse(dim1.hasCommonKeywords(dim2));
    }

    /**
     * Test for Dimension.hasCommonKeywords
     */
    @Test
    public void hasNoCommonKeywords()
    {
        Dimension dim1 = new Dimension();
        dim1.add(kw1);
        dim1.add(kw2);
        dim1.add(kw4);
        Dimension dim2 = new Dimension();
        dim2.add(kw3);
        assertFalse(dim1.hasCommonKeywords(dim2));
    }
    /**
     * Test for Dimension.hasCommonKeywords
     */
    @Test
    public void hasOneCommonKeywords()
    {
        Dimension dim1 = new Dimension();
        dim1.add(kw1);
        dim1.add(kw2);
        dim1.add(kw4);
        Dimension dim2 = new Dimension();
        dim2.add(kw3);
        dim2.add(kw4);
        assertTrue(dim1.hasCommonKeywords(dim2));
    }

    /**
    * Test for Dimension.equals
    */
    @Test public void equalsWithSameDim()
    {
        Dimension dim = new Dimension();

        dim.add(kw1);
        assertTrue(dim.equals(dim));
    }

    /**
    * Test for Dimension.equals
    */
    @Test public void equalsWithEquivalentDims()
    {
        Dimension dim1 = new Dimension();
        Dimension dim2 = new Dimension();

        dim1.add(kw1);
        dim2.add(kw1);
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
        Dimension dim1 = new Dimension();
        Dimension dim2 = new Dimension();

        dim1.add(kw1);
        dim1.add(kw2);

        dim2.add(kw3);
        assertFalse(dim1.equals(dim2));

        dim2.add(kw1);
        assertFalse(dim1.equals(dim2));
    }
}
