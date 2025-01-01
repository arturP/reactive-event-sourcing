package io.artur.bank.customer.application.projection;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.javadsl.EventsByPersistenceIdQuery;
import io.artur.bank.customer.domain.AccountEvent;

import java.util.concurrent.CompletionStage;

public class AccountViewProjection {

    private final AccountViewRepository accountViewRepository;
    private final ActorSystem<?> actorSystem;
    private final EventsByPersistenceIdQuery byPersistenceIdQuery;

    public AccountViewProjection(EventsByPersistenceIdQuery byPersistenceIdQuery, ActorSystem<?> actorSystem, AccountViewRepository accountViewRepository) {
        this.byPersistenceIdQuery = byPersistenceIdQuery;
        this.actorSystem = actorSystem;
        this.accountViewRepository = accountViewRepository;
    }

    public void run(String persistenceId) {
        long from = 0;
        long to = Long.MAX_VALUE;
        byPersistenceIdQuery.eventsByPersistenceId(persistenceId, from, to)
                .mapAsync(1, this::processEvent)
                .run(actorSystem);
    }

    private CompletionStage<Done> processEvent(EventEnvelope eventEnvelope) {
        if (eventEnvelope.event() instanceof AccountEvent accountEvent) {
            return switch (accountEvent) {
                case AccountEvent.AccountCreated accountCreated -> accountViewRepository.save(accountCreated.accountId(), accountCreated.initialAccount().balance().value());
                case AccountEvent.AccountWithdrawn balanceDecreased -> accountViewRepository.decrementBalance(balanceDecreased.accountId(), balanceDecreased.amount().value());
                case AccountEvent.AccountBalanced balanceIncreased -> accountViewRepository.incrementBalance(balanceIncreased.accountId(), balanceIncreased.balance().value());
                default -> throw new IllegalStateException("Unexpected value: " + accountEvent);
            };
        } else {
            throw new IllegalStateException("Unrecognized event type");
        }
    }
}
