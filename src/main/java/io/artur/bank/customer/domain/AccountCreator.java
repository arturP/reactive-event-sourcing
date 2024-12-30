package io.artur.bank.customer.domain;

import io.artur.bank.base.domain.Clock;
import io.vavr.control.Either;
import io.artur.bank.customer.domain.AccountEvent.AccountCreated;
import io.artur.bank.customer.domain.AccountCommand.CreateAccount;

import java.math.BigDecimal;

import static io.artur.bank.customer.domain.AccountCommandError.INVALID_ACCOUNT_NAME;
import static io.artur.bank.customer.domain.AccountCommandError.INVALID_ACCOUNT_TYPE;

public class AccountCreator {

    public static Either<AccountCommandError, AccountCreated> create(CreateAccount createAccount, Clock clock) {
        if (createAccount.name().isBlank()) {
            return Either.left(INVALID_ACCOUNT_NAME);
        } else if (createAccount.type() == null || createAccount.type().isBlank()) {
            return Either.left(INVALID_ACCOUNT_TYPE);
        } else {
            var initialAccount = new InitialAccount(createAccount.accountId(), createAccount.name(), createAccount.type(), Money.of(BigDecimal.ZERO), clock.now());
            return Either.right(new AccountCreated(createAccount.accountId(), clock.now(), initialAccount));

        }

    }
}
