package io.artur.bank.customer;

import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import io.artur.bank.base.domain.Clock;
import io.artur.bank.customer.application.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerConfiguration {


    @Bean
    AccountService accountService(ClusterSharding sharding, Clock clock) {
        return new AccountService(sharding, clock);
    }
}
