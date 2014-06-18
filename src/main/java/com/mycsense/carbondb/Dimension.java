package com.mycsense.carbondb; 

import java.util.HashSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class Dimension
{
    HashSet<Keyword> keywords;

    public Dimension() {
        keywords = new HashSet<Keyword>();
    }

    public Dimension(Keyword... pKeywords) {
        keywords = new HashSet<Keyword>();
        for (Keyword kw: pKeywords) {
            keywords.add(kw);
        }
    }

    public Dimension(Dimension dimension) {
        keywords = new HashSet<Keyword>(dimension.keywords);
    }

    public int size() {
        return keywords.size();
    }

    public boolean add(Keyword keyword) {
        return keywords.add(keyword);
    }

    public String toString() {
        return keywords.toString();
    }

    public boolean contains(Keyword keyword) {
        return keywords.contains(keyword);
    }

    public boolean isEmpty() {
        return keywords.isEmpty();
    }

    public Object[] toArray() {
        return keywords.toArray();
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