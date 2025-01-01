package io.artur.bank.customer.application.projection;

import akka.actor.typed.ActorSystem;
import akka.persistence.query.Offset;
import akka.projection.Projection;
import akka.projection.ProjectionId;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.javadsl.SourceProvider;
import akka.projection.jdbc.javadsl.JdbcProjection;
import io.artur.bank.customer.domain.AccountEvent;

import javax.sql.DataSource;
import java.time.Duration;

import static akka.projection.HandlerRecoveryStrategy.retryAndFail;
import static java.time.Duration.ofSeconds;

public class AccountViewProjection {

    public static final ProjectionId PROJECTION_ID = ProjectionId.of("account-events", "account-view");

    private final ActorSystem<?> actorSystem;
    private final DataSource dataSource;
    private final AccountViewEventHandler accountViewEventHandler;
    private final int saveOffsetAfterEnvelopes = 100;
    private final Duration saveOffsetAfterDuration = Duration.ofMillis(500);

    public AccountViewProjection(ActorSystem<?> actorSystem, DataSource dataSource, AccountViewEventHandler accountViewEventHandler) {
        this.actorSystem = actorSystem;
        this.dataSource = dataSource;
        this.accountViewEventHandler = accountViewEventHandler;
    }

    public Projection<EventEnvelope<AccountEvent>> create(SourceProvider<Offset, EventEnvelope<AccountEvent>> sourceProvider) {
        return JdbcProjection.atLeastOnceAsync(
                        PROJECTION_ID,
                        sourceProvider,
                        () -> new DataSourceJdbcSession(dataSource),
                        () -> accountViewEventHandler,
                        actorSystem)
                .withSaveOffset(saveOffsetAfterEnvelopes, saveOffsetAfterDuration)
                .withRecoveryStrategy(retryAndFail(4, ofSeconds(5))) //could be configured in application.conf
                .withRestartBackoff(ofSeconds(3), ofSeconds(30), 0.1d); //could be configured in application.conf
    }
}
