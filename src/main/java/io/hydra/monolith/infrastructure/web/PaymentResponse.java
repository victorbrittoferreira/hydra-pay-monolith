package io.hydra.monolith.infrastructure.web;

import io.hydra.monolith.application.usecase.ProcessPaymentResult;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID transactionId,
        String status,
        UUID debitAccountId,
        UUID creditAccountId,
        BigDecimal amount,
        String currency
) {
    public static PaymentResponse from(ProcessPaymentResult result) {
        return new PaymentResponse(
                result.transactionId(),
                result.status().name(),
                result.debitAccountId(),
                result.creditAccountId(),
                result.amount(),
                result.currency().name()
        );
    }
}
