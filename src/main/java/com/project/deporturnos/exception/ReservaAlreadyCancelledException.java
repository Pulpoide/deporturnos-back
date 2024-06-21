package com.project.deporturnos.exception;

public class ReservaAlreadyCancelledException extends RuntimeException{
    public ReservaAlreadyCancelledException(String message) {
        super(message);
    }
}
