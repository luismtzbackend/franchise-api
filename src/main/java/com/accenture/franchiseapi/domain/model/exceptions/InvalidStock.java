package com.accenture.franchiseapi.domain.model.exceptions;

public class InvalidStock extends RuntimeException {

    private final int attemptedValue;

    public InvalidStock(int attemptedValue) {
        super("El stock no puede ser negativo. Valor intentado: " + attemptedValue);
        this.attemptedValue = attemptedValue;
    }

    public int getAttemptedValue() {
        return attemptedValue;
    }
}
