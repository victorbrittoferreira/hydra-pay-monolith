package io.hydra.monolith.application.usecase;

import io.hydra.monolith.domain.Currency;
import io.hydra.monolith.domain.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentResult(
        UUID transactionId,
        TransactionStatus status,
        UUID debitAccountId,
        UUID creditAccountId,
        BigDecimal amount,
        Currency currency
) {
}
