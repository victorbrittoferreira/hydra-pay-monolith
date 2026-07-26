package io.hydra.monolith.application.service;

public class InvalidCurrencyException extends RuntimeException {
    public InvalidCurrencyException(String currency) {
        super("Unsupported currency: " + currency);
    }
}
