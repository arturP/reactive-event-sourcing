package io.artur.bank.base;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.SpawnProtocol;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.artur.bank.base.application.SpawningBehavior;
import io.artur.bank.base.domain.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BaseConfiguration {

    @Bean
    Config config() {
        return ConfigFactory.load();
    }

    @Bean(destroyMethod = "terminate")
    public ActorSystem<SpawnProtocol.Command> actorSystem(Config config) {
        return ActorSystem.create(SpawningBehavior.create(), "es-bank", config);
    }

    @Bean
    ClusterSharding clusterSharding(ActorSystem<?> actorSystem) {
        return ClusterSharding.get(actorSystem);
    }

    @Bean
    Clock clock() {
        return new Clock.SystemClock();
    }
}
