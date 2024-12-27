package io.artur.bank.customer.api;

import io.artur.bank.customer.domain.Account;

import java.math.BigDecimal;

public record AccountResponse(String id, String name, BigDecimal balance, String currency, String accountType) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId().id().toString(), account.getAccountName(), account.getBalance().value(), account.getBalance().currency(), account.getAccountType().name());
    }
}
