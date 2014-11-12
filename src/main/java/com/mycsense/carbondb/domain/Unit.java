package com.mycsense.carbondb.domain;

public class Unit {
    protected String URI;
    protected String symbol;
    protected String ref;

    public Unit(String URI, String symbol, String ref) {
        this.URI = URI;
        this.symbol = symbol;
        this.ref = ref;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String toString()
    {
        return symbol;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
