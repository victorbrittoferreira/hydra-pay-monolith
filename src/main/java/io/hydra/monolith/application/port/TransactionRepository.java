package io.hydra.monolith.application.port;

import io.hydra.monolith.domain.Transaction;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    void save(Transaction transaction);

    Optional<Transaction> findById(UUID id);
}
