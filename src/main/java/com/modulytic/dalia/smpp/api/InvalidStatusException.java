package com.modulytic.dalia.smpp.api;

public class InvalidStatusException extends Exception {
    public InvalidStatusException(String error) {
        super(error);
    }
}
