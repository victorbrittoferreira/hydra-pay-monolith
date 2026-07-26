package io.hydra.monolith.application.usecase;

import io.hydra.monolith.domain.AccountId;
import io.hydra.monolith.domain.Currency;
import io.hydra.monolith.domain.IdempotencyKey;
import io.hydra.monolith.domain.Money;

import java.util.Objects;

public record ProcessPaymentCommand(
        AccountId debitAccountId,
        AccountId creditAccountId,
        Money amount,
        Currency currency,
        IdempotencyKey idempotencyKey
) {
    public ProcessPaymentCommand {
        Objects.requireNonNull(debitAccountId, "debitAccountId");
        Objects.requireNonNull(creditAccountId, "creditAccountId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
    }
}
