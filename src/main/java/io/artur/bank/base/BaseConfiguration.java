package io.artur.bank.base;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.artur.bank.base.application.VoidBehaviour;
import io.artur.bank.base.domain.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import akka.persistence.testkit.PersistenceTestKitPlugin;

@Configuration
public class BaseConfiguration {

    @Bean
    Config config() {
        return PersistenceTestKitPlugin.config().withFallback(ConfigFactory.load());
    }

    @Bean(destroyMethod = "terminate")
    public ActorSystem<Void> actorSystem(Config config) {
        return ActorSystem.create(VoidBehaviour.create(), "es-bank", config);
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
