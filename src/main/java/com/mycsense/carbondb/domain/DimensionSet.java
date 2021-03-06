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

package com.mycsense.carbondb.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.mycsense.carbondb.domain.dimension.Orientation;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * <p>This class stores all the dimensions of a group. It contains a set of keywords
 * and has an orientation for each dimension.</p>
 * <p>Dimensions inside a DimensionSet should not be modified
 * after they have been added to the dimensionSet.</p>
 */
public class DimensionSet {
    public HashSet<Dimension> dimensions;
    public HashMap<String, Orientation> dimensionOrientations;

    /**
     * Construct a new DimensionSet with an empty dimension set.
     */
    public DimensionSet() {
        dimensions = new HashSet<>();
        dimensionOrientations = new HashMap<>();
    }

    /**
     * Construct a new DimensionSet with all the given dimensions.
     * @param dimensions an undefined number of dimensions
     */
    public DimensionSet(Dimension... dimensions) {
        this();
        Collections.addAll(this.dimensions, dimensions);
    }

    /**
     * Construcut a new DimensionSet from another DimensionSet.
     * @param dimSet another DimensionSet
     */
    public DimensionSet(DimensionSet dimSet) {
        this();
        for (Dimension dim: dimSet.dimensions) {
            dimensions.add(dim);
        }
    }

    /**
     * @return number of dimensions in this set
     */
    public int size() {
        return dimensions.size();
    }

    /**
     * Add a dimension to the set
     *
     * @param dimension dimension to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified
     * dimension
     */
    public boolean add(Dimension dimension) {
        return dimensions.add(dimension);
    }

    /**
     * Remove a dimension from the set
     *
     * @param dimension dimension to be removed
     * @return <tt>true</tt> if the set contained the specified dimension
     */
    public boolean remove(Dimension dimension) {
        return dimensions.remove(dimension);
    }

    /**
     * @return string representation of the set
     */
    public String toString() {
        return dimensions.toString();
    }

    /**
     * @return <tt>true</tt> if the set does not contains any dimension
     */
    public boolean isEmpty() {
        return dimensions.isEmpty();
    }

    /**
     * @return an array containing all the dimensions
     */
    public Object[] toArray() {
        return dimensions.toArray();
    }

    /**
     * @param dimension dimension that needs an orientation
     * @param orientation orientation of the dimension
     */
    public void setDimensionOrientation(Dimension dimension, Orientation orientation) {
        dimensionOrientations.put(dimension.getId(), orientation);
    }

    /**
     * @param dimension dimension
     * @return orientation of the dimension
     */
    public Orientation getDimensionOrientation(Dimension dimension) {
        return getDimensionOrientation(dimension.getId());
    }

    /**
     *
     * @param id id of a dimension
     * @return orientation of the dimension, if none set, returns Orientation.NONE
     */
    public Orientation getDimensionOrientation(String id) {
        if (dimensionOrientations.containsKey(id)) {
            return dimensionOrientations.get(id);
        }
        return Orientation.NONE;
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

    /**
     * Checks if a dimension is in the set.
     *
     * @param dim dimension to test
     * @return <tt>true</tt> if the dimension is in the set
     */
    public boolean contains(Dimension dim) {
        return dimensions.contains(dim);
    }

    /**
     * <p>Returns a new DimensionSet containing the cartesian product
     * of all the dimensions contained in the set.</p>
     *
     * One keyword from every dimension
     * <p>Every dimension is </p>
     *
     * @return set containing the car
     */
    public DimensionSet combinations()
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
            for (Keyword keyword: dimensions[index].keywords) {
                for (Dimension dim: _getCombinations(index+1, dimensions).dimensions) {
                    dim.addKeyword(keyword);
                    ret.add(dim);
                }
            }
        }
        return ret;
    }

    public Dimension commonKeywords(DimensionSet dimSet)
    {
        Dimension commonKeywords = new Dimension();
        Dimension hashTableRhs = dimSet.keywordsHashTable();

        for (Dimension dimension: dimensions) {
            for (Keyword keyword: dimension.keywords) {
                if (hashTableRhs.contains(keyword)) {
                    commonKeywords.addKeyword(keyword);
                }
            }
        }

        return commonKeywords;
    }

    public Integer alpha(DimensionSet dimSet)
    {
        Integer alpha = 0;
        Dimension hashTableRhs = dimSet.keywordsHashTable();

        for (Dimension dimension: dimensions) {
            if (dimension.hasCommonKeywords(hashTableRhs)) {
                alpha++;
            }
        }
        return alpha;
    }

    public class UnionResult {
        public DimensionSet dimSet;
        public Integer alpha;
        public Dimension commonKeywords;

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

        HashMap<Keyword, Dimension> hashTableRhs = new HashMap<>();
        DimensionSet unusedDimsInRhs = new DimensionSet();

        for (Dimension dimension: dimSet.dimensions) {
            unusedDimsInRhs.add(dimension);
            for (Keyword keyword: dimension.keywords) {
                hashTableRhs.put(keyword, dimension);
            }
        }
        for (Dimension dimension: dimensions) {
            Dimension dimResultTemp = new Dimension();
            for (Keyword keyword: dimension.keywords) {
                if (hashTableRhs.containsKey(keyword)) {
                    unusedDimsInRhs.remove(hashTableRhs.get(keyword));
                    dimResultTemp.addKeyword(keyword);
                    r.commonKeywords.addKeyword(keyword);
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

    public Dimension keywordsHashTable()
    {
        Dimension hashTable = new Dimension();
        for (Dimension dimension: dimensions) {
            for (Keyword keyword: dimension.keywords) {
                hashTable.addKeyword(keyword);
            }
        }
        return hashTable;
    }

    public int numberOfIntersections(Dimension dimension) {
        int intersections = 0;
        for (Dimension dim: dimensions) {
            if (dim.hasCommonKeywords(dimension)) {
                intersections++;
            }
        }
        return intersections;
    }

    public boolean isCompatible(DimensionSet dimSet) {
        for (Dimension dim: dimensions) {
            if (dimSet.numberOfIntersections(dim) > 1)
                return false;
        }
        for (Dimension dim: dimSet.dimensions) {
            if (numberOfIntersections(dim) > 1)
                return false;
        }
        return true;
    }
}