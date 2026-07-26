package io.hydra.monolith.domain;

import java.util.Objects;

public record IdempotencyKey(String value) {
    public IdempotencyKey {
        Objects.requireNonNull(value, "value");
        if (value.isBlank() || value.length() > 80) {
            throw new IllegalArgumentException("idempotency key must be 1-80 characters");
        }
    }
}
