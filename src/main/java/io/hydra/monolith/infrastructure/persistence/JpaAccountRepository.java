package io.hydra.monolith.infrastructure.persistence;

import io.hydra.monolith.application.port.AccountRepository;
import io.hydra.monolith.domain.Account;
import io.hydra.monolith.domain.AccountId;
import io.hydra.monolith.domain.Money;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaAccountRepository implements AccountRepository {
    private final AccountJpaRepository accountJpaRepository;

    public JpaAccountRepository(AccountJpaRepository accountJpaRepository) {
        this.accountJpaRepository = accountJpaRepository;
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return accountJpaRepository.findById(id.value())
                .map(entity -> new Account(
                        new AccountId(entity.getId()),
                        new Money(entity.getBalance(), entity.getCurrency())
                ));
    }

    @Override
    public void saveAll(List<Account> accounts) {
        Instant now = Instant.now();
        List<AccountEntity> entities = accounts.stream()
                .map(account -> {
                    AccountEntity entity = accountJpaRepository.findById(account.id().value())
                            .orElseGet(() -> new AccountEntity(
                                    account.id().value(),
                                    account.balance().amount(),
                                    account.balance().currency(),
                                    now,
                                    now
                            ));
                    entity.setBalance(account.balance().amount());
                    entity.setUpdatedAt(now);
                    return entity;
                })
                .toList();
        accountJpaRepository.saveAll(entities);
    }
}
