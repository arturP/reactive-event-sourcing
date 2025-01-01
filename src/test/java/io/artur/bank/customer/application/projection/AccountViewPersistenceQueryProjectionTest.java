package io.artur.bank.customer.application.projection;

import akka.Done;
import akka.actor.ActorSystem;
import akka.actor.typed.javadsl.Adapter;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.PersistenceQuery;
import akka.persistence.testkit.PersistenceTestKitPlugin;
import akka.persistence.testkit.query.javadsl.PersistenceTestKitReadJournal;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.artur.bank.base.domain.Clock;
import io.artur.bank.customer.application.AccountService;
import io.artur.bank.customer.domain.AccountEvent;
import io.artur.bank.customer.domain.AccountId;
import io.artur.bank.customer.domain.Money;
import io.artur.bank.customer.infrastructure.InMemoryAccountViewRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static io.artur.bank.customer.application.AccountEntity.persistenceId;
import static org.assertj.core.api.Assertions.assertThat;

class AccountViewPersistenceQueryProjectionTest {

    private static Config config = PersistenceTestKitPlugin.config().withFallback(ConfigFactory.load());
    private static ActorSystem system = ActorSystem.create("es-bank", config);
    private ClusterSharding sharding = ClusterSharding.get(Adapter.toTyped(system));
    private Clock clock = new Clock.SystemClock();
    private AccountService accountService = new AccountService(sharding, clock);
    private AccountViewRepository accountViewRepository = new InMemoryAccountViewRepository();
    private PersistenceTestKitReadJournal readJournal = PersistenceQuery.get(system)
            .getReadJournalFor(PersistenceTestKitReadJournal.class, PersistenceTestKitReadJournal.Identifier());

    @AfterAll
    public static void cleanUp() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    void shouldGetAvailableAccountViewsUsingByPersistenceId() {
        //given
        var accountId1 = AccountId.of();
        var accountId2 = AccountId.of();

        accountService.createAccount(accountId1, "FirstAccount", "SAVINGS");
        accountService.withdraw(accountId1, Money.of(BigDecimal.valueOf(50)));
        accountService.createAccount(accountId2, "SecondAccount", "SAVINGS");

        //when
        readJournal.eventsByPersistenceId(persistenceId(accountId1).id(), 0, Long.MAX_VALUE)
                .mapAsync(1, this::processEvent)
                .run(system);

        readJournal.eventsByPersistenceId(persistenceId(accountId2).id(), 0, Long.MAX_VALUE)
                .mapAsync(1, this::processEvent)
                .run(system);

        //then
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var accountViews = accountViewRepository.findAll().toCompletableFuture().get();
            assertThat(accountViews).hasSize(2);
            assertThat(accountViews).extracting(AccountView::accountId).containsExactlyInAnyOrder(accountId1.toString(), accountId2.toString());
            assertThat(accountViews).extracting(AccountView::balance).containsExactlyInAnyOrder(BigDecimal.valueOf(50), BigDecimal.ZERO);
        });
    }

    private CompletionStage<Done> processEvent(EventEnvelope eventEnvelope) {
        if (eventEnvelope.event() instanceof AccountEvent accountEvent) {
            return switch (accountEvent) {
                case AccountEvent.AccountCreated accountCreated ->
                        accountViewRepository.save(accountCreated.accountId(), accountCreated.initialAccount().balance().value());
                case AccountEvent.AccountWithdrawn balanceDecreased ->
                        accountViewRepository.decrementBalance(balanceDecreased.accountId(), balanceDecreased.amount().value());
                case AccountEvent.AccountBalanced balanceIncreased ->
                        accountViewRepository.incrementBalance(balanceIncreased.accountId(), balanceIncreased.balance().value());
                default -> throw new IllegalStateException("Unexpected value: " + accountEvent);
            };
        } else {
            throw new IllegalStateException("Unrecognized event type");
        }
    }
}
