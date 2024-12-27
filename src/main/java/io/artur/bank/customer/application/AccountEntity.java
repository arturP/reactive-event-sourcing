package io.artur.bank.customer.application;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.ReplyEffect;
import io.artur.bank.base.domain.Clock;
import io.artur.bank.customer.domain.Account;
import io.artur.bank.customer.domain.AccountCommand;
import io.artur.bank.customer.domain.AccountEvent;
import io.artur.bank.customer.domain.AccountId;

public class AccountEntity extends EventSourcedBehaviorWithEnforcedReplies<AccountEntityCommand, AccountEvent, Account> {

    public static final EntityTypeKey<AccountEntityCommand> ACCOUNT_ENTITY_TYPE_KEY =
            EntityTypeKey.create(AccountEntityCommand.class, "Account");

    private final AccountId accountId;
    private final Clock clock;
    private final ActorContext<AccountEntityCommand> context;

    private AccountEntity(PersistenceId persistenceId, AccountId accountId, Clock clock, ActorContext<AccountEntityCommand> context) {
        super(persistenceId);
        this.accountId = accountId;
        this.clock = clock;
        this.context = context;
    }

    public static Behavior<AccountEntityCommand> create(AccountId accountId, Clock clock) {
        return Behaviors.setup(context-> {
            PersistenceId persistenceId = PersistenceId.of("Account", accountId.id().toString());
            context.getLog().info("AccountEntity {} initialization started", accountId);
            return new AccountEntity(persistenceId, accountId, clock, context);
        });
    }

    @Override
    public Account emptyState() {
        return Account.create(accountId, clock);
    }

    @Override
    public CommandHandlerWithReply<AccountEntityCommand, AccountEvent, Account> commandHandler() {
        return newCommandHandlerWithReplyBuilder().forStateType(Account.class)
                .onCommand(AccountEntityCommand.GetAccount.class, this::returnState)
                .onCommand(AccountEntityCommand.AccountCommandEnvelope.class, this::handleAccountCommand)
                .build();
    }

    private ReplyEffect<AccountEvent, Account> handleAccountCommand(
            Account account,
            AccountEntityCommand.AccountCommandEnvelope envelope) {
        AccountCommand accountCommand = envelope.command();
        return account.process(accountCommand, clock).fold(
            error -> {
                context.getLog().info("Command rejected: {} with {}", accountCommand, error);
                return Effect().reply(envelope.replyTo(), new AccountEntityResponse.CommandRejected(error));
            },
                events -> {
                    context.getLog().info("Command processed: {} with {}", accountCommand, events);
                    return Effect().persist(events.toJavaList()).thenReply(envelope.replyTo(), s -> new AccountEntityResponse.CommandProcessed());
                }
        );
    }

    @Override
    public EventHandler<Account, AccountEvent> eventHandler() {
        return newEventHandlerBuilder()
                .forStateType(Account.class)
                .onAnyEvent(Account::apply);
    }

    private ReplyEffect<AccountEvent, Account> returnState(Account account, AccountEntityCommand.GetAccount getAccount) {
        return Effect().reply(getAccount.replyTo(), account);
    }
}
