package io.artur.bank.customer;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.SpawnProtocol;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.jdbc.query.javadsl.JdbcReadJournal;
import akka.persistence.query.Offset;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.eventsourced.javadsl.EventSourcedProvider;
import akka.projection.javadsl.SourceProvider;
import com.zaxxer.hikari.HikariDataSource;
import io.artur.bank.base.domain.Clock;
import io.artur.bank.customer.application.AccountEntity;
import io.artur.bank.customer.application.AccountService;
import io.artur.bank.customer.application.projection.AccountViewEventHandler;
import io.artur.bank.customer.application.projection.AccountViewProjection;
import io.artur.bank.customer.application.projection.AccountViewRepository;
import io.artur.bank.customer.application.projection.ProjectionLauncher;
import io.artur.bank.customer.domain.AccountEvent;
import io.artur.bank.customer.infrastructure.InMemoryAccountViewRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class CustomerConfiguration {

    private final ActorSystem<SpawnProtocol.Command> system;
    private final ClusterSharding sharding;
    private final Clock clock;

    public CustomerConfiguration(ActorSystem<SpawnProtocol.Command> system, ClusterSharding sharding, Clock clock) {
        this.system = system;
        this.sharding = sharding;
        this.clock = clock;
    }

    @Bean
    AccountService accountService() {
        return new AccountService(sharding, clock);
    }

    @Bean
    AccountViewRepository accountViewRepository() {
        return new InMemoryAccountViewRepository();
    }

    @Bean(initMethod = "runProjections")
    public ProjectionLauncher projectionLauncher(AccountViewRepository accountViewRepository) {
        AccountViewEventHandler accountViewEventHandler = new AccountViewEventHandler(accountViewRepository);
        SourceProvider<Offset, EventEnvelope<AccountEvent>> sourceProvider =  EventSourcedProvider
                .eventsByTag(system, JdbcReadJournal.Identifier(), AccountEntity.ACCOUNT_EVENT_TAG);
        AccountViewProjection accountViewProjection = new AccountViewProjection(system, dataSource(), accountViewEventHandler);
        ProjectionLauncher projectionLauncher = new ProjectionLauncher(system);
        projectionLauncher.withLocalProjections(accountViewProjection.create(sourceProvider));
        return projectionLauncher;
    }

    public DataSource dataSource() {
        var hikariDataSource = new HikariDataSource();
        hikariDataSource.setPoolName("projection-data-source");
        hikariDataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
        hikariDataSource.setUsername("admin");
        hikariDataSource.setPassword("admin");
        // https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
        hikariDataSource.setMaximumPoolSize(5);
        hikariDataSource.setRegisterMbeans(true);
        return hikariDataSource;
    }
}
