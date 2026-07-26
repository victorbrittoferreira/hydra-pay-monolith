package io.hydra.monolith.infrastructure.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        @NotNull UUID debitAccountId,
        @NotNull UUID creditAccountId,
        @NotNull @Positive BigDecimal amount,
        String currency
) {
}
