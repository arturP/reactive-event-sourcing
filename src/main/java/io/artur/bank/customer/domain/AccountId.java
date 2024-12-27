package io.artur.bank.customer.domain;

import java.io.Serializable;
import java.util.UUID;

public record AccountId(UUID id) implements Serializable {

    public static AccountId of() {
        return of(UUID.randomUUID());
    }

    public static AccountId of(UUID id) {
        return new AccountId(id);
    }
}
