package io.hydra.monolith.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Transaction(
        UUID id,
        AccountId debitAccountId,
        AccountId creditAccountId,
        Money amount,
        TransactionStatus status,
        IdempotencyKey idempotencyKey,
        Instant createdAt
) {
    public Transaction {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(debitAccountId, "debitAccountId");
        Objects.requireNonNull(creditAccountId, "creditAccountId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}
