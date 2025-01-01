package io.artur.bank.customer.application.projection;

import akka.Done;
import io.artur.bank.customer.domain.AccountId;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface AccountViewRepository {

    CompletionStage<List<AccountView>> findAll();

    CompletionStage<Done> save(AccountId accountId, BigDecimal balance);

    CompletionStage<Done> decrementBalance(AccountId accountId, BigDecimal amount);

    CompletionStage<Done> incrementBalance(AccountId accountId, BigDecimal amount);
}
