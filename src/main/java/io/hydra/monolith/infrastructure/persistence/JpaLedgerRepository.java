package io.hydra.monolith.infrastructure.persistence;

import io.hydra.monolith.application.port.LedgerRepository;
import io.hydra.monolith.domain.LedgerEntry;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaLedgerRepository implements LedgerRepository {
    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

    public JpaLedgerRepository(LedgerEntryJpaRepository ledgerEntryJpaRepository) {
        this.ledgerEntryJpaRepository = ledgerEntryJpaRepository;
    }

    @Override
    public void saveAll(List<LedgerEntry> entries) {
        List<LedgerEntryEntity> entities = entries.stream()
                .map(entry -> new LedgerEntryEntity(
                        entry.id(),
                        entry.transactionId(),
                        entry.accountId().value(),
                        entry.entryType(),
                        entry.amount().amount(),
                        entry.amount().currency(),
                        entry.createdAt()
                ))
                .toList();
        ledgerEntryJpaRepository.saveAll(entities);
    }
}
