package com.mycsense.carbondb; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Dimensions inside a DimensionSet should not be modified
 * after they have been added to the dimensionSet
 */
class DimensionSet
{
    HashSet<Dimension> dimensions;

    public DimensionSet() {
        dimensions = new HashSet<Dimension>();
    }

    public DimensionSet(Dimension... dimensions) {
        this.dimensions = new HashSet<Dimension>();
        for (Dimension dim: dimensions) {
            this.dimensions.add(dim);
        }
    }

    public int size() {
        return dimensions.size();
    }

    public boolean add(Dimension dimension) {
        return dimensions.add(dimension);
    }

    public boolean remove(Dimension dimension) {
        return dimensions.remove(dimension);
    }

    public String toString() {
        return dimensions.toString();
    }

    public boolean isEmpty() {
        return dimensions.isEmpty();
    }

    public Object[] toArray() {
        return dimensions.toArray();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DimensionSet))
            return false;
        if (obj == this)
            return true;

        DimensionSet rhs = (DimensionSet) obj;
        return new EqualsBuilder()
                  .append(dimensions, rhs.dimensions)
                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(29, 41)
                  .append(dimensions)
                  .toHashCode();
    }

    public boolean contains(Dimension dim) {
        return dimensions.contains(dim);
    }

    public DimensionSet getCombinations()
    {
        if (dimensions.size() == 0) {
            return new DimensionSet();
        }
        return _getCombinations(0, dimensions.toArray(new Dimension[0]));
    }

    protected DimensionSet _getCombinations(int index, Dimension... dimensions)
    {
        DimensionSet ret = new DimensionSet();
        if (index == dimensions.length) {
            ret.add(new Dimension());
        }
        else {
            for (Resource keyword: dimensions[index].keywords) {
                for (Dimension dim: _getCombinations(index+1, dimensions).dimensions) {
                    dim.add(keyword);
                    ret.add(dim);
                }
            }
        }
        return ret;
    }

    public Dimension getCommonKeywords(DimensionSet dimSet)
    {
        Dimension commonKeywords = new Dimension();
        HashSet<Resource> hashTableRhs = new HashSet<Resource>();
        for (Dimension dimension: dimSet.dimensions) {
            for (Resource keyword: dimension.keywords) {
                hashTableRhs.add(keyword);
            }
        }
        for (Dimension dimension: dimensions) {
            for (Resource keyword: dimension.keywords) {
                if (hashTableRhs.contains(keyword)) {
                    commonKeywords.add(keyword);
                }
            }
        }

        return commonKeywords;
    }
}