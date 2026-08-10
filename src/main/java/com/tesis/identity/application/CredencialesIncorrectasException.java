package com.tesis.identity.application;

public class CredencialesIncorrectasException extends RuntimeException {

    public CredencialesIncorrectasException(String message) {
        super(message);
    }
}