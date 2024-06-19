package com.project.deporturnos.exception;

public class TurnoAlreadyReserved extends RuntimeException{
    public TurnoAlreadyReserved(String message) {
        super(message);
    }
}
