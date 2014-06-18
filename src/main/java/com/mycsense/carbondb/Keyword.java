package com.mycsense.carbondb; 

class Keyword
{
    String name;

    public Keyword() {
        name = new String();
    }

    public Keyword(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name.toString();
    }
}