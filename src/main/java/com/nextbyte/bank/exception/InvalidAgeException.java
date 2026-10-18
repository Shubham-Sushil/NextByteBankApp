package com.nextbyte.bank.exception;

// Extending Exception makes this a CHECKED exception in Java
public class InvalidAgeException extends Exception {
    public InvalidAgeException(String message) {
        super(message);
    }
}