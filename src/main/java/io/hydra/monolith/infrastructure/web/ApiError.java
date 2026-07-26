package io.hydra.monolith.infrastructure.web;

public record ApiError(
        String code,
        String message,
        String correlationId
) {
}
