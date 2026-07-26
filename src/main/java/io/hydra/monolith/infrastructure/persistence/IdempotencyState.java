package io.hydra.monolith.infrastructure.persistence;

public enum IdempotencyState {
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
