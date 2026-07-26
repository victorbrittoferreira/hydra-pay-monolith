package io.hydra.monolith.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("amount scale must be <= 2");
        }
    }

    public Money normalized() {
        return new Money(amount.setScale(2, RoundingMode.HALF_UP), currency);
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(amount.add(other.amount), currency).normalized();
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency).normalized();
    }

    private void validateSameCurrency(Money other) {
        if (currency != other.currency) {
            throw new IllegalArgumentException("currency mismatch");
        }
    }
}
