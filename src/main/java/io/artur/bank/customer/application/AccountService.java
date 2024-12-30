package io.artur.bank.customer.application;

import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import io.artur.bank.base.domain.Clock;
import io.artur.bank.customer.domain.Account;
import io.artur.bank.customer.domain.AccountCommand;
import io.artur.bank.customer.domain.AccountId;
import io.artur.bank.customer.domain.Money;
import io.vavr.control.Option;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static io.artur.bank.customer.application.AccountEntity.ACCOUNT_ENTITY_TYPE_KEY;

public class AccountService {

    private final ClusterSharding sharding;
    private Duration askTimeout = Duration.ofSeconds(10);

    public AccountService(final ClusterSharding sharding, final Clock clock) {
        this.sharding = sharding;
        this.sharding.init(Entity.of(ACCOUNT_ENTITY_TYPE_KEY, entityContext -> {
            AccountId accountId = AccountId.of(UUID.fromString(entityContext.getEntityId()));
            return AccountEntity.create(accountId, clock);
        }));
    }

    public CompletionStage<AccountEntityResponse> createAccount(AccountId accountId, String name, String type) {
        return processCommand(new AccountCommand.CreateAccount(accountId, name, type));
    }

    public CompletionStage<Option<Account>> findAccountBy(AccountId accountId) {
        return getAccountEntityRef(accountId).ask(replyTo -> new AccountEntityCommand.GetAccount(replyTo), askTimeout);
    }

    public CompletionStage<AccountEntityResponse> deposit(AccountId accountId, Money amount) {
        return processCommand(new AccountCommand.Deposit(accountId, amount));
    }

    public CompletionStage<AccountEntityResponse> withdraw(AccountId accountId, Money amount) {
        return processCommand(new AccountCommand.Withdraw(accountId, amount));
    }

    private CompletionStage<AccountEntityResponse> processCommand(AccountCommand command) {
        return getAccountEntityRef(command.accountId())
                .ask(replyTo -> new AccountEntityCommand.AccountCommandEnvelope(command, replyTo), askTimeout);
    }

    private EntityRef<AccountEntityCommand> getAccountEntityRef(AccountId accountId) {
        return sharding.entityRefFor(ACCOUNT_ENTITY_TYPE_KEY, accountId.id().toString());
    }
}
