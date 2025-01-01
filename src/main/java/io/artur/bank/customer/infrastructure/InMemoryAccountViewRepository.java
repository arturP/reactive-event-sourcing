package io.artur.bank.customer.infrastructure;

import akka.Done;
import io.artur.bank.customer.application.projection.AccountView;
import io.artur.bank.customer.application.projection.AccountViewRepository;
import io.artur.bank.customer.domain.AccountId;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class InMemoryAccountViewRepository implements AccountViewRepository {

    private final ConcurrentHashMap<AccountId, AccountView> store = new ConcurrentHashMap<>();

    @Override
    public CompletionStage<List<AccountView>> findAll() {
        return completedFuture(store.values().stream().toList());
    }

    @Override
    public CompletionStage<Done> save(AccountId accountId, BigDecimal balance) {
        return supplyAsync(() -> {
            store.put(accountId, new AccountView(accountId.toString(), balance));
            return Done.done();
        });
    }

    @Override
    public CompletionStage<Done> decrementBalance(AccountId accountId, BigDecimal amount) {
        return supplyAsync(() -> {
            store.computeIfPresent(accountId, (id, view) -> new AccountView(view.accountId(), view.balance().subtract(amount)));
            return Done.done();
        });
    }

    @Override
    public CompletionStage<Done> incrementBalance(AccountId accountId, BigDecimal amount) {
        return supplyAsync(() -> {
            store.computeIfPresent(accountId, (id, view) -> new AccountView(view.accountId(), view.balance().add(amount)));
            return Done.done();
        });
    }
}
