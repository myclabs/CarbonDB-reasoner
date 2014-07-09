package com.mycsense.carbondb;

public class Value {
    public Double value;
    public Double uncertainty;

    public Value (Double value, Double uncertainty) {
        this.value = value;
        this.uncertainty = uncertainty;
    }
}