package io.artur.bank.customer.application;

import akka.actor.typed.ActorRef;
import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.persistence.testkit.javadsl.EventSourcedBehaviorTestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.artur.bank.base.domain.Clock;
import io.artur.bank.customer.domain.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.artur.bank.customer.domain.AccountCommandGenerator.randomDeposit;
import static org.assertj.core.api.Assertions.assertThat;

class AccountEntityTest {

    public static final Config UNIT_TEST_AKKA_CONFIGURATION = ConfigFactory.parseString("""
                akka.actor.enable-additional-serialization-bindings = on
                akka.actor.allow-java-serialization = on
                akka.actor.warn-about-java-serializer-usage = off
                akka.loglevel = INFO
            """);

    private Clock clock = new FixedClock(Instant.now());

    private static final ActorTestKit testKit =
            ActorTestKit.create(EventSourcedBehaviorTestKit.config().withFallback(UNIT_TEST_AKKA_CONFIGURATION));

    @AfterAll
    public static void cleanUp() {
        testKit.shutdownTestKit();
    }

    @Test
    void shouldDepositAccount() {
        var accountId = AccountId.of();
        EventSourcedBehaviorTestKit<AccountEntityCommand, AccountEvent, Account> accountEntityKit =
                EventSourcedBehaviorTestKit.create(testKit.system(), AccountEntity.create(accountId, clock));
        var accountDeposit = randomDeposit(accountId);

        var result = accountEntityKit.<AccountEntityResponse>runCommand(replyTo -> toEnvelope(accountDeposit, replyTo));

        assertThat(result.reply()).isInstanceOf(AccountEntityResponse.CommandProcessed.class);
        assertThat(result.event()).isInstanceOf(AccountEvent.AccountDeposited.class);
        var accountDeposited = (AccountEvent.AccountDeposited) result.events().getFirst();
        assertThat(accountDeposited.accountId()).isEqualTo(accountId);
    }

    private AccountEntityCommand.AccountCommandEnvelope toEnvelope(AccountCommand accountCommand, ActorRef<AccountEntityResponse> replyTo) {
        return new AccountEntityCommand.AccountCommandEnvelope(accountCommand, replyTo);
    }
}