package io.hydra.monolith.domain;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(AccountId accountId) {
        super("Insufficient funds for account " + accountId.value());
    }
}
