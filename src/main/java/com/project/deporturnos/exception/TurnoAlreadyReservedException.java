package com.project.deporturnos.exception;

public class TurnoAlreadyReservedException extends RuntimeException{
    public TurnoAlreadyReservedException(String message) {
        super(message);
    }
}
