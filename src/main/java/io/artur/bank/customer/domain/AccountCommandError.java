package io.artur.bank.customer.domain;

public enum AccountCommandError {
    ACCOUNT_NOT_FOUND,
    ACCOUNT_ALREADY_EXISTS,
    ACCOUNT_NOT_EXIST,
    INVALID_ACCOUNT_NAME,
    INVALID_ACCOUNT_TYPE,
    INSUFFICIENT_FUNDS,
    INVALID_AMOUNT,
    ACCOUNT_CLOSED
}
