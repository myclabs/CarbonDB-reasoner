package com.mycsense.carbondb; 

import java.util.HashMap;
import java.util.HashSet;

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
        Dimension hashTableRhs = dimSet.getKeywordsHashTable();

        for (Dimension dimension: dimensions) {
            for (Resource keyword: dimension.keywords) {
                if (hashTableRhs.contains(keyword)) {
                    commonKeywords.add(keyword);
                }
            }
        }

        return commonKeywords;
    }

    public Integer alpha(DimensionSet dimSet)
    {
        Integer alpha = 0;
        Dimension hashTableRhs = dimSet.getKeywordsHashTable();

        for (Dimension dimension: dimensions) {
            if (dimension.hasCommonKeywords(hashTableRhs)) {
                alpha++;
            }
        }
        return alpha;
    }

    public class UnionResult {
        DimensionSet dimSet;
        Integer alpha;
        Dimension commonKeywords;

        public String toString()
        {
            return dimSet.toString();
        }
    }

    public UnionResult union(DimensionSet dimSet)
    {
        UnionResult r = new UnionResult();

        r.dimSet = new DimensionSet();
        r.alpha = 0;
        r.commonKeywords = new Dimension();
        Integer dimIndex = -1;

        HashMap<Resource, Dimension> hashTableRhs = new HashMap<Resource, Dimension>();
        DimensionSet unusedDimsInRhs = new DimensionSet();

        for (Dimension dimension: dimSet.dimensions) {
            unusedDimsInRhs.add(dimension);
            for (Resource keyword: dimension.keywords) {
                hashTableRhs.put(keyword, dimension);
            }
        }
        for (Dimension dimension: dimensions) {
            Dimension dimResultTemp = new Dimension();
            for (Resource keyword: dimension.keywords) {
                if (hashTableRhs.containsKey(keyword)) {
                    unusedDimsInRhs.remove(hashTableRhs.get(keyword));
                    dimResultTemp.add(keyword);
                    r.commonKeywords.add(keyword);
                }
            }
            if (dimResultTemp.isEmpty()) {
                r.dimSet.add(dimension);
            }
            else {
                r.dimSet.add(dimResultTemp);
                r.alpha++;
            }
        }
        for (Dimension dimension: unusedDimsInRhs.dimensions) {
            r.dimSet.add(dimension);
        }

        return r;
    }

    public Dimension getKeywordsHashTable()
    {
        Dimension hashTable = new Dimension();
        for (Dimension dimension: dimensions) {
            for (Resource keyword: dimension.keywords) {
                hashTable.add(keyword);
            }
        }
        return hashTable;
    }
}