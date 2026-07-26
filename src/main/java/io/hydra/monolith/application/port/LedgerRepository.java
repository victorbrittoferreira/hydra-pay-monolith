package io.hydra.monolith.application.port;

import io.hydra.monolith.domain.LedgerEntry;

import java.util.List;

public interface LedgerRepository {
    void saveAll(List<LedgerEntry> entries);
}
