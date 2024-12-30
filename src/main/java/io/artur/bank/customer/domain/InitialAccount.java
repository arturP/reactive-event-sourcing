package io.artur.bank.customer.domain;

import java.time.Instant;

public record InitialAccount(AccountId accountId, String name, String type, Money balance, Instant at) {
}
