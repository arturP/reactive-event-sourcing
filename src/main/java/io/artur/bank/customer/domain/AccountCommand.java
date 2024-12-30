package io.artur.bank.customer.domain;

import java.io.Serializable;

public sealed interface AccountCommand extends Serializable {
    AccountId accountId();

    record CreateAccount(AccountId accountId, String name, String type) implements AccountCommand {
    }

    record Deposit(AccountId accountId, Money amount) implements AccountCommand {
    }

    record Withdraw(AccountId accountId, Money amount) implements AccountCommand {
    }

    record GetAccountBalance(AccountId accountId) implements AccountCommand {
    }

    record CloseAccount(AccountId accountId) implements AccountCommand {
    }
}
