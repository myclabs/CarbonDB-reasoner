package com.mycsense.carbondb; 

class MacroRelation {
    Group source;
    Group coeff;
    Group destination;

    public MacroRelation(Group source, Group coeff, Group destination) {
        this.source = source;
        this.coeff = coeff;
        this.destination = destination;
    }

    public String toString() {
        return source + " x " + coeff + " -> " + destination;
    }
}