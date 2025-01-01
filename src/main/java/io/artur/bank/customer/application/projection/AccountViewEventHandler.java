package io.artur.bank.customer.application.projection;

import akka.Done;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.javadsl.Handler;
import io.artur.bank.customer.domain.AccountEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

public class AccountViewEventHandler extends Handler<EventEnvelope<AccountEvent>> {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AccountViewRepository accountViewRepository;

    public AccountViewEventHandler(AccountViewRepository accountViewRepository) {
        this.accountViewRepository = accountViewRepository;
    }
    
    @Override
    public CompletionStage<Done> process(EventEnvelope<AccountEvent> accountEventEventEnvelope) {
        log.info("Processing: {}", accountEventEventEnvelope.event());
        return switch (accountEventEventEnvelope.event()) {
            case AccountEvent.AccountCreated accountCreated ->
                    accountViewRepository.save(accountCreated.accountId(), accountCreated.initialAccount().balance().value());
            case AccountEvent.AccountBalanced accountBalanced -> accountViewRepository.incrementBalance(accountBalanced.accountId(), accountBalanced.balance().value());
            case AccountEvent.AccountWithdrawn accountWithdrawn -> accountViewRepository.decrementBalance(accountWithdrawn.accountId(), accountWithdrawn.amount().value());
            default -> throw new IllegalStateException("Unexpected value: " + accountEventEventEnvelope.event());
        };
    }
}
