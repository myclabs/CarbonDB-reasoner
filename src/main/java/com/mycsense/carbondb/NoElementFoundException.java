package com.mycsense.carbondb;

public class NoElementFoundException extends Exception {
      public NoElementFoundException() {}

      public NoElementFoundException(String message)
      {
         super(message);
      }
}