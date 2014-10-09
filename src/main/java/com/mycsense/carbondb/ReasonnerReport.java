package com.mycsense.carbondb; 

import java.util.ArrayList;

public class ReasonnerReport {
    public ArrayList<String> errors = new ArrayList<>();
    public ArrayList<String> warnings = new ArrayList<>();

    public void addError(String error) {
    	errors.add(error);
    }

    public void addWarning(String warning) {
    	if (!warnings.contains(warning))
            warnings.add(warning);
    }
}