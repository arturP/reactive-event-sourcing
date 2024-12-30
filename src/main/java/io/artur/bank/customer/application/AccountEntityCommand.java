package io.artur.bank.customer.application;

import akka.actor.typed.ActorRef;
import io.artur.bank.customer.domain.Account;
import io.artur.bank.customer.domain.AccountCommand;
import io.vavr.control.Option;

import java.io.Serializable;

public sealed interface AccountEntityCommand extends Serializable {
    record AccountCommandEnvelope(AccountCommand command, ActorRef<AccountEntityResponse> replyTo) implements AccountEntityCommand{
    }

    record GetAccount(ActorRef<Option<Account>> replyTo) implements AccountEntityCommand {
    }
}
