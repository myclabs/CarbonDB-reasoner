package com.mycsense.carbondb;

public class IncompatibleDimSetException extends Exception {
      public IncompatibleDimSetException() {}

      public IncompatibleDimSetException(String message)
      {
         super(message);
      }
}