package io.artur.bank.customer.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public record Money(BigDecimal value, String currency) implements Serializable {
    private static final String DEFAULT_CURRENCY = "USD";

    public static Money of(BigDecimal value) {
        return new Money(value, DEFAULT_CURRENCY);
    }

    public Money add(Money other) {
        return new Money(value.add(other.value), currency);
    }

    public Money subtract(Money other) {
        return new Money(value.subtract(other.value), currency);
    }

    public boolean isGraterThan(Money other) {
        throwExceptionIfNull(other);
        return value.compareTo(other.value) > 0;
    }

    public boolean isEqual(Money other) {
        throwExceptionIfNull(other);
        return value.compareTo(other.value) == 0;
    }

    private void throwExceptionIfNull(Money money) {
        if (money == null) {
            throw new IllegalArgumentException("Provided Money object is null");
        }
    }
}
