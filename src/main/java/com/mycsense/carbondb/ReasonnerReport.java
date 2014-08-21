package com.mycsense.carbondb; 

import java.util.ArrayList;

public class ReasonnerReport {
    public ArrayList<String> errors = new ArrayList<String>();
    public ArrayList<String> warnings = new ArrayList<String>();

    public void addError(String error) {
    	errors.add(error);
    }

    public void addWarning(String warning) {
    	warnings.add(warning);
    }
}