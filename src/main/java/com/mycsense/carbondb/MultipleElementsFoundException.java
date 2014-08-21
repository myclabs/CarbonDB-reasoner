package com.mycsense.carbondb;

public class MultipleElementsFoundException extends Exception {
      public MultipleElementsFoundException() {}

      public MultipleElementsFoundException(String message)
      {
         super(message);
      }
}