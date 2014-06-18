package com.mycsense.carbondb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Unit test for DimensionSet.
 */
public class DimensionSetTest 
{
    Resource kw1, kw2, kw3, kw4;
    Dimension dim12, dim34;

    @Before
    public void setUp() {
        kw1 = ResourceFactory.createResource("kw1");
        kw2 = ResourceFactory.createResource("kw2");
        kw3 = ResourceFactory.createResource("kw3");
        kw4 = ResourceFactory.createResource("kw4");
        dim12 = new Dimension();
        dim12.add(kw1);
        dim12.add(kw2);
        dim34 = new Dimension();
        dim34.add(kw3);
        dim34.add(kw4);
    }

    /**
    * Test for DimensionSet.equals
    */
    @Test
    public void equalsWithEmptyDimensionSetB()
    {
        DimensionSet dimSet1 = new DimensionSet();
        dimSet1.add(dim12);
        dimSet1.add(dim34);
        DimensionSet dimSet2 = new DimensionSet();

        assertFalse(dimSet1.equals(dimSet2));
    }

    /**
    * Test for DimensionSet.equals
    */
    @Test
    public void equalsWithSameDimensionSet()
    {
        DimensionSet dimSet1 = new DimensionSet();
        dimSet1.add(dim12);
        dimSet1.add(dim34);

        assertTrue(dimSet1.equals(dimSet1));
    }

    /**
    * Test for DimensionSet.equals
    */
    @Test
    public void equalsWithEquivalentDimensionSets()
    {
        DimensionSet dimSet1 = new DimensionSet();
        dimSet1.add(dim12);
        dimSet1.add(dim34);

        DimensionSet dimSet2 = new DimensionSet();
        Dimension dim212 = new Dimension();
        dim212.add(kw1);
        dim212.add(kw2);
        dimSet2.add(dim212);
        Dimension dim234 = new Dimension();
        dim234.add(kw3);
        dim234.add(kw4);
        dimSet2.add(dim234);

        assertTrue(dimSet1.equals(dimSet2));
    }

    /**
    * Test for DimensionSet.equals
    */
    @Test
    public void equalsWithDifferentDimensionSets()
    {
        DimensionSet dimSet1 = new DimensionSet();
        dimSet1.add(dim12);
        dimSet1.add(dim34);

        DimensionSet dimSet2 = new DimensionSet();
        Dimension dim212 = new Dimension();
        dim212.add(kw1);
        dimSet2.add(dim212);

        assertFalse(dimSet1.equals(dimSet2));

        Dimension dim234 = new Dimension();
        dim234.add(kw3);
        dimSet2.add(dim234);
        assertFalse(dimSet1.equals(dimSet2));

        dim234.add(kw4);
        assertFalse(dimSet1.equals(dimSet2));
    }

    /**
     * Test for DimensionSet.getCombinations
     */
    @Test
    public void getCombinationsWithEmptySet()
    {
        DimensionSet dimSet = new DimensionSet();
        assertEquals(0, dimSet.getCombinations().size());
    }

    /**
     * Test for DimensionSet.getCombinations
     */
    @Test
    public void getCombinationsWithOneDimensionDimSet()
    {
        DimensionSet dimSet = new DimensionSet();
        dimSet.add(dim12);

        DimensionSet dimSetResult = dimSet.getCombinations();

        Dimension expectedDim1 = new Dimension();
        Dimension expectedDim2 = new Dimension();
        expectedDim1.add(kw1);
        expectedDim2.add(kw2);
        DimensionSet expectedDimSet = new DimensionSet();
        expectedDimSet.add(expectedDim1);
        expectedDimSet.add(expectedDim2);

        assertEquals(2, dimSetResult.size());
        System.out.println("------ Before DimensionSet.equals call");
        assertTrue(dimSetResult.equals(expectedDimSet));
    }

    /**
     * Test for DimensionSet.getCombinations
     */
    @Test
    public void getCombinations()
    {
        DimensionSet dimSet = new DimensionSet();
        dimSet.add(dim12);
        dimSet.add(dim34);

        DimensionSet dimSetResult = dimSet.getCombinations();

        DimensionSet expectedDimSet = new DimensionSet();
        Dimension expectedDim1 = new Dimension();
        expectedDim1.add(kw1);
        expectedDim1.add(kw3);
        expectedDimSet.add(expectedDim1);
        Dimension expectedDim2 = new Dimension();
        expectedDim2.add(kw1);
        expectedDim2.add(kw4);
        expectedDimSet.add(expectedDim2);
        Dimension expectedDim3 = new Dimension();
        expectedDim3.add(kw2);
        expectedDim3.add(kw3);
        expectedDimSet.add(expectedDim3);
        Dimension expectedDim4 = new Dimension();
        expectedDim4.add(kw2);
        expectedDim4.add(kw4);
        expectedDimSet.add(expectedDim4);

        System.out.println("dimSetResult = " + dimSetResult);
        assertEquals(4, dimSetResult.size());
        assertTrue(dimSetResult.equals(expectedDimSet));
    }

    /**
     * Test for DimensionSet.getCommonKeywords
     */
    @Test
    public void getCommonKeywordsWithEmptyDimSet()
    {
        DimensionSet dimSet1 = new DimensionSet();
        dimSet1.add(dim12);

        DimensionSet dimSet2 = new DimensionSet();

        assertEquals(0, dimSet1.getCommonKeywords(dimSet2).size());
    }

    /**
     * Test for DimensionSet.getCommonKeywords
     */
    @Test
    public void getCommonKeywordsWithDifferentDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet();
        dimSet1.add(dim12);

        DimensionSet dimSet2 = new DimensionSet();
        dimSet2.add(dim34);

        assertEquals(0, dimSet1.getCommonKeywords(dimSet2).size());
    }

    /**
     * Test for DimensionSet.getCommonKeywords
     */
    @Test
    public void getCommonKeywordsWithOverlappingDimSets()
    {
        DimensionSet dimSet1 = new DimensionSet();
        dimSet1.add(dim12);

        DimensionSet dimSet2 = new DimensionSet();
        Dimension dim1 = new Dimension();
        Dimension dim2 = new Dimension();
        dim1.add(kw1);
        dim1.add(kw3);
        dim2.add(kw2);
        dim2.add(kw4);
        dimSet2.add(dim1);
        dimSet2.add(dim2);

        Dimension dimResult = dimSet1.getCommonKeywords(dimSet2);
        Dimension expectedDim = new Dimension();

        expectedDim.add(kw1);
        expectedDim.add(kw2);

        assertEquals(2, dimResult.size());
        assertTrue(expectedDim.equals(dimResult));
    }

    @Test public void testForbiddenActionWithHashCode()
    {
        DimensionSet dimSet = new DimensionSet();
        dimSet.add(dim12);
        assertTrue(dimSet.contains(dim12));
        dim12.add(kw3);
        // The hashcode for the dimension has changed,
        // contains uses this hashcode
        // thus we cannot find our dimension in the dimSet
        assertFalse(dimSet.contains(dim12));
    }
}
