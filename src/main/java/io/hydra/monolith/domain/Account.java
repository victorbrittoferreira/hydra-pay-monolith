package io.hydra.monolith.domain;

import java.util.Objects;

public record Account(AccountId id, Money balance) {
    public Account {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(balance, "balance");
    }

    public Account debit(Money amount) {
        validateAmount(amount);
        if (balance.amount().compareTo(amount.amount()) < 0) {
            throw new InsufficientFundsException(id);
        }
        return new Account(id, balance.subtract(amount));
    }

    public Account credit(Money amount) {
        validateAmount(amount);
        return new Account(id, balance.add(amount));
    }

    private void validateAmount(Money amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (balance.currency() != amount.currency()) {
            throw new IllegalArgumentException("currency mismatch");
        }
    }
}
