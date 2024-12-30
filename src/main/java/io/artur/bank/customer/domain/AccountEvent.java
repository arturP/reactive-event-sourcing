package io.artur.bank.customer.domain;

import java.io.Serializable;
import java.time.Instant;

public sealed interface AccountEvent extends Serializable {
    AccountId accountId();

    Instant at();

    record AccountCreated(AccountId accountId, Instant at, InitialAccount initialAccount) implements AccountEvent {
    }

    record AccountDeposited(AccountId accountId, Money amount, Money balance, Instant at) implements AccountEvent {
    }

    record AccountWithdrawn(AccountId accountId, Money amount, Money balance, Instant at) implements AccountEvent {
    }

    record AccountClosed(AccountId accountId, Money balance, Instant at) implements AccountEvent {
    }

    record AccountBalanced(AccountId accountId, Money balance, Instant at) implements AccountEvent {
    }
}
