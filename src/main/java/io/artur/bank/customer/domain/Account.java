package io.artur.bank.customer.domain;

import io.artur.bank.customer.domain.AccountCommand.Deposit;
import io.artur.bank.customer.domain.AccountCommand.GetAccountBalance;
import io.artur.bank.customer.domain.AccountCommand.Withdraw;
import io.artur.bank.customer.domain.AccountCommand.CloseAccount;
import io.artur.bank.customer.domain.AccountEvent.AccountBalanced;
import io.artur.bank.customer.domain.AccountEvent.AccountDeposited;
import io.artur.bank.customer.domain.AccountEvent.AccountWithdrawn;
import io.artur.bank.customer.domain.AccountEvent.AccountClosed;

import io.artur.bank.base.domain.Clock;
import io.vavr.control.Either;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import io.vavr.collection.List;

public class Account implements Serializable {

    private final AccountId id;
    private final String accountName;
    private Money balance;
    private final AccountType accountType;
    private final Instant createdAt;
    private Status status;

    public Account(AccountId id,
            String accountName,
            Money balance,
            AccountType accountType,
            Instant createdAt,
            Status status) {
        this.id = id;
        this.accountName = accountName;
        this.balance = balance;
        this.accountType = accountType;
        this.createdAt = createdAt;
        this.status = status;
    }

    public AccountId getId() {
        return id;
    }

    public String getAccountName() {
        return accountName;
    }

    public Money getBalance() {
        return balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public static Account create(AccountId accountId, Clock clock) {
        return new Account(accountId, "Account " + accountId, Money.of(BigDecimal.ZERO), AccountType.SAVINGS, clock.now(), Status.ACTIVE);
    }

    public static Account create(AccountId id, String name, AccountType type, Clock clock) {
        return new Account(id, name, Money.of(BigDecimal.ZERO), type, clock.now(), Status.ACTIVE);
    }

    public Either<AccountCommandError, List<AccountEvent>> process(AccountCommand command, Clock clock) {
        return switch (command) {
            case Deposit deposit -> handleDeposit(deposit, clock);
            case Withdraw withdraw -> handleWithdraw(withdraw, clock);
            case CloseAccount closeAccount -> handleCloseAccount(closeAccount, clock);
            case GetAccountBalance getAccountBalance -> handleGetAccountBalance(getAccountBalance, clock);
        };
    }

    public Account apply(AccountEvent event) {
        return switch (event) {
            case AccountDeposited accountDeposited -> applyDeposited(accountDeposited);
            case AccountWithdrawn accountWithdrawn -> applyWithdrawn(accountWithdrawn);
            case AccountClosed accountClosed -> applyClosed(accountClosed);
            case AccountBalanced accountBalanced -> applyBalanced(accountBalanced);
            default -> this;
        };
    }

    private Either<AccountCommandError, List<AccountEvent>> handleDeposit(Deposit deposit, Clock clock) {
        if (status == Status.CLOSED) {
            return Either.left(AccountCommandError.ACCOUNT_CLOSED);

        }
        // now we assume this same currency
        balance = balance.add(deposit.amount());
        return Either.right(List.of(new AccountDeposited(id, deposit.amount(), balance, clock.now())));
    }

    private Either<AccountCommandError, List<AccountEvent>> handleWithdraw(Withdraw withdraw, Clock clock) {
        if (status == Status.CLOSED) {
            return Either.left(AccountCommandError.ACCOUNT_CLOSED);

        }
        if (withdraw.amount().isGraterThan(balance)) {
            return Either.left(AccountCommandError.INSUFFICIENT_FUNDS);
        }
        balance.subtract(withdraw.amount());
        return Either.right(List.of(new AccountWithdrawn(id, withdraw.amount(), balance, clock.now())));
    }

    private Either<AccountCommandError, List<AccountEvent>> handleCloseAccount(CloseAccount closeAccount, Clock clock) {
        if (status == Status.CLOSED) {
            return Either.left(AccountCommandError.ACCOUNT_CLOSED);
        }
        return Either.right(List.of(new AccountClosed(id, balance, clock.now())));
    }

    private Either<AccountCommandError, List<AccountEvent>> handleGetAccountBalance(
            GetAccountBalance getAccountBalance,
            Clock clock) {
        if (status == Status.CLOSED) {
            return Either.left(AccountCommandError.ACCOUNT_CLOSED);

        }
        return Either.right(List.of(new AccountBalanced(id, balance, clock.now())));
    }

    private Account applyDeposited(AccountDeposited event) {
        checkAccountIfMatches(event);
        return new Account(id, accountName, event.amount(), accountType, createdAt, status);
    }

    private Account applyWithdrawn(AccountWithdrawn event) {
        checkAccountIfMatches(event);
        return new Account(id, accountName, balance.subtract(event.amount()), accountType, createdAt, status);
    }

    private Account applyClosed(AccountClosed event) {
        checkAccountIfMatches(event);
        return new Account(id, accountName, event.balance(), accountType, createdAt, Status.CLOSED);
    }

    private Account applyBalanced(AccountBalanced event) {
        checkAccountIfMatches(event);
        return new Account(id, accountName, event.balance(), accountType, createdAt, status);
    }

    private void checkAccountIfMatches(AccountEvent event) {
        if (!id.equals(event.accountId())) {
            throw new IllegalArgumentException("Account id does not match");
        }
    }
}
