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
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Dimension {
    protected String id;
    public TreeSet<Keyword> keywords;
    public HashMap<Integer, String> keywordsPosition;

    public Dimension() {
        this("");
    }

    public Dimension(String id) {
        this.id = id;
        keywords = new TreeSet<>();
        keywordsPosition = new HashMap<>();
    }

    public Dimension(Keyword... pKeywords) {
        this();
        Collections.addAll(keywords, pKeywords);
    }

    public Dimension(Dimension dimension) {
        this();
        keywords = new TreeSet<>(dimension.keywords);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int size() {
        return keywords.size();
    }

    public boolean addKeyword(Keyword keyword) {
        return keywords.add(keyword);
    }

    public void addKeywordPosition(Integer position, String keywordURI) {
        keywordsPosition.put(position, keywordURI);
    }

    public String toString() {
        return StringUtils.join(keywords, "+");
    }

    public boolean contains(Keyword keyword) {
        return keywords.contains(keyword);
    }

    public boolean isEmpty() {
        return keywords.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        // Alternative: use Guava (from Google)
        if (!(obj instanceof Dimension))
            return false;
        if (obj == this)
            return true;

        Dimension rhs = (Dimension) obj;
        return new EqualsBuilder()
                  .append(keywords, rhs.keywords)
                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 67)
                  .append(keywords)
                  .toHashCode();
    }

    public Boolean hasCommonKeywords(Dimension dimension)
    {
        for (Keyword keyword: keywords) {
            if (dimension.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}