package io.artur.bank.customer.application;

import io.artur.bank.customer.domain.AccountCommandError;

import java.io.Serializable;

public sealed interface AccountEntityResponse extends Serializable {
    final class CommandProcessed implements AccountEntityResponse {
    }

    record CommandRejected(AccountCommandError error) implements AccountEntityResponse {
    }
}
