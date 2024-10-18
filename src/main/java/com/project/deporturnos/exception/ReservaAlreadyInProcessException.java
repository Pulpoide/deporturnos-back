package com.project.deporturnos.exception;

public class ReservaAlreadyInProcessException extends RuntimeException {
    public ReservaAlreadyInProcessException(String message) {
        super(message);
    }
}
