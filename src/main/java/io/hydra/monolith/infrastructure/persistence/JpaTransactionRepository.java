package io.hydra.monolith.infrastructure.persistence;

import io.hydra.monolith.application.port.TransactionRepository;
import io.hydra.monolith.domain.AccountId;
import io.hydra.monolith.domain.IdempotencyKey;
import io.hydra.monolith.domain.Money;
import io.hydra.monolith.domain.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaTransactionRepository implements TransactionRepository {
    private final TransactionJpaRepository transactionJpaRepository;

    public JpaTransactionRepository(TransactionJpaRepository transactionJpaRepository) {
        this.transactionJpaRepository = transactionJpaRepository;
    }

    @Override
    public void save(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity(
                transaction.id(),
                transaction.debitAccountId().value(),
                transaction.creditAccountId().value(),
                transaction.amount().amount(),
                transaction.amount().currency(),
                transaction.status(),
                transaction.idempotencyKey().value(),
                transaction.createdAt()
        );
        transactionJpaRepository.save(entity);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return transactionJpaRepository.findById(id)
                .map(entity -> new Transaction(
                        entity.getId(),
                        new AccountId(entity.getDebitAccountId()),
                        new AccountId(entity.getCreditAccountId()),
                        new Money(entity.getAmount(), entity.getCurrency()),
                        entity.getStatus(),
                        new IdempotencyKey(entity.getIdempotencyKey()),
                        entity.getCreatedAt()
                ));
    }
}
