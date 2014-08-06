package com.mycsense.carbondb;

public class Value {
    public Double value;
    public Double uncertainty;

    public Value (Double value, Double uncertainty) {
        this.value = value;
        this.uncertainty = uncertainty;
    }

    public Value add(Value v2) {
        value += v2.value;
        if (Math.abs(value) == 0)
            uncertainty = 0.0;
        else
            uncertainty = Math.sqrt(Math.pow(value * uncertainty, 2)
                                    + Math.pow(v2.value * v2.uncertainty, 2))
                          / Math.abs(value);
        return this;
    }
}