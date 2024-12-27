package io.artur.bank.customer.domain;

public enum AccountCommandError {
    ACCOUNT_NOT_FOUND,
    INSUFFICIENT_FUNDS,
    INVALID_AMOUNT,
    ACCOUNT_CLOSED
}
