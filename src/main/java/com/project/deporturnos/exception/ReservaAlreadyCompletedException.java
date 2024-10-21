package com.project.deporturnos.exception;

public class ReservaAlreadyCompletedException extends RuntimeException {
    public ReservaAlreadyCompletedException(String message) {
        super(message);
    }
}
