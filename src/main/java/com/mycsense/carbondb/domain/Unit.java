package com.mycsense.carbondb.domain;

public class Unit {
    protected String URI;
    protected String symbol;

    public Unit(String URI, String symbol) {
        this.URI = URI;
        this.symbol = symbol;
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
}
