package io.artur.bank.customer.application;

import akka.actor.ActorSystem;
import akka.actor.typed.javadsl.Adapter;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.testkit.PersistenceTestKitPlugin;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.artur.bank.base.domain.Clock;
import io.artur.bank.customer.domain.AccountId;
import io.artur.bank.customer.domain.Money;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import static io.artur.bank.customer.application.Blocking.await;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AccountServiceTest {

    private static Config config = PersistenceTestKitPlugin.config().withFallback(ConfigFactory.load());
    private static ActorSystem system = ActorSystem.create("es-bank", config);
    private ClusterSharding sharding = ClusterSharding.get(Adapter.toTyped(system));
    private Clock clock = new Clock.SystemClock();
    private AccountService accountService = new AccountService(sharding, clock);

    @AfterAll
    public static void cleanUp() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    void shouldCreateAccount() throws ExecutionException, InterruptedException {
        //given
        var accountName = "Test";
        var accountType = "SAVINGS";

        //when
        var result = await(accountService.createAccount(accountName, accountType).toCompletableFuture());

        //then
        assertThat(result).isInstanceOf(AccountEntityResponse.CommandProcessed.class);
    }

    @Test
    void shouldDepositAccount() throws ExecutionException, InterruptedException {
        //given
        var accountId = AccountId.of();
        var amount = Money.of(new BigDecimal(100));

        //when
        var result = await(accountService.deposit(accountId, amount).toCompletableFuture());

        //then
        assertThat(result).isInstanceOf(AccountEntityResponse.CommandProcessed.class);
    }

    @Test
    void shouldFindAccountBy() throws ExecutionException, InterruptedException {
        //given
        var accountId = AccountId.of();

        //when
        var result = accountService.findAccountBy(accountId).toCompletableFuture().get();

        //then
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get().getId()).isEqualTo(accountId);
    }
}
