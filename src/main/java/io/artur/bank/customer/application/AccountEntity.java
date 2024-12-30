package io.artur.bank.customer.application;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import io.artur.bank.base.domain.Clock;
import io.artur.bank.customer.application.AccountEntityResponse.CommandProcessed;
import io.artur.bank.customer.application.AccountEntityResponse.CommandRejected;
import io.artur.bank.customer.domain.*;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;

import static io.artur.bank.customer.domain.AccountCommandError.ACCOUNT_NOT_EXIST;

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
        return null;
    }

    @Override
    public CommandHandlerWithReply<AccountEntityCommand, AccountEvent, Account> commandHandler() {
        var builder = newCommandHandlerWithReplyBuilder();
        builder.forNullState()
                .onCommand(AccountEntityCommand.GetAccount.class, this::returnEmptyState)
                .onCommand(AccountEntityCommand.AccountCommandEnvelope.class, this::handleAccountCreation);

        builder.forStateType(Account.class)
                .onCommand(AccountEntityCommand.GetAccount.class, this::returnState)
                .onCommand(AccountEntityCommand.AccountCommandEnvelope.class, this::handleAccountCommand);

        return builder.build();
    }

    private ReplyEffect<AccountEvent, Account> handleAccountCommand(
            Account account,
            AccountEntityCommand.AccountCommandEnvelope envelope) {
        final AccountCommand accountCommand = envelope.command();
        return account.process(accountCommand, clock).fold(
            error -> {
                context.getLog().info("Command rejected: {} with {}", accountCommand, error);
                return Effect().reply(envelope.replyTo(), new CommandRejected(error));
            },
                events -> {
                    context.getLog().info("Command processed: {} with {}", accountCommand, events);
                    return Effect().persist(events.toJavaList()).thenReply(envelope.replyTo(), s -> new CommandProcessed());
                }
        );
    }

    @Override
    public EventHandler<Account, AccountEvent> eventHandler() {
        final EventHandlerBuilder<Account, AccountEvent> builder = newEventHandlerBuilder();

        builder.forNullState()
                .onEvent(AccountEvent.AccountCreated.class, Account::create);

        builder.forStateType(Account.class)
                .onAnyEvent(Account::apply);

        return builder.build();
    }

    private ReplyEffect<AccountEvent, Account> returnState(Account account, AccountEntityCommand.GetAccount getAccount) {
        return Effect().reply(getAccount.replyTo(), Option.of(account));
    }

    private ReplyEffect<AccountEvent, Account> returnEmptyState(AccountEntityCommand.GetAccount getAccount) {
        return Effect().reply(getAccount.replyTo(), Option.none());
    }

    private ReplyEffect<AccountEvent, Account> handleAccountCreation(AccountEntityCommand.AccountCommandEnvelope envelope) {
        final AccountCommand accountCommand = envelope.command();
        if (accountCommand instanceof AccountCommand.CreateAccount createAccount) {
            Either<AccountCommandError, List<AccountEvent>> processingResult = AccountCreator.create(createAccount, clock).map(List::of);
            return handleResult(envelope, processingResult);
        } else {
            context.getLog().warn("Account {} not created", accountId);
            return Effect().reply(envelope.replyTo(), new CommandRejected(ACCOUNT_NOT_EXIST));
        }
    }

    private ReplyEffect<AccountEvent, Account> handleResult(
            AccountEntityCommand.AccountCommandEnvelope envelope,
            Either<AccountCommandError, List<AccountEvent>> processingResult) {
        final AccountCommand accountCommand = envelope.command();
        return processingResult.fold(
            error -> {
                context.getLog().info("Command rejected: {} with {}", accountCommand, error);
                return Effect().reply(envelope.replyTo(), new CommandRejected(error));
            },
            events -> {
                context.getLog().info("Command handled: {}", accountCommand);
                return Effect().persist(events.toJavaList()).thenReply(envelope.replyTo(), s -> new CommandProcessed());
            }
        );
    }
}
