package io.hydra.monolith.application.port;

import io.hydra.monolith.domain.Account;
import io.hydra.monolith.domain.AccountId;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(AccountId id);

    void saveAll(List<Account> accounts);
}
