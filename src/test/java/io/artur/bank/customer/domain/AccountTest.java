package io.artur.bank.customer.domain;

import io.artur.bank.base.domain.Clock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import io.vavr.collection.List;

import io.artur.bank.customer.domain.AccountEvent.AccountDeposited;
import io.artur.bank.customer.domain.AccountCommand.CloseAccount;

import static io.artur.bank.customer.domain.AccountCommandGenerator.*;
import static io.artur.bank.customer.domain.DomainGenerator.randomAccount;
import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    private final Clock clock = new FixedClock(Instant.now());

    @Test
    void shouldCreateTheAccount() {
        final var accountId = AccountId.of();
        final var createAccount = randomCreateAccount(accountId);

        final var accountCreated = AccountCreator.create(createAccount, clock).get();
        final var account = Account.create(accountCreated);

        assertThat(account.getId()).isEqualTo(accountId);
        assertThat(account.getAccountName()).isEqualTo(createAccount.name());
        assertThat(account.getBalance().isEqual(Money.of(BigDecimal.ZERO))).isTrue();
        assertThat(account.getAccountType()).isEqualTo(AccountType.valueOf(createAccount.type()));
        assertThat(account.getCreatedAt()).isEqualTo(clock.now());
    }

    @Test
    void shouldDepositAnAccount() {
        final var randomAccount = randomAccount();
        final var depositCommand = randomDeposit(randomAccount.getId());
        final var expectedBalance = randomAccount.getBalance().add(depositCommand.amount());

        final var events = randomAccount.process(depositCommand, clock).get();

        assertThat(events).hasSize(1);
        assertThat(events).containsOnly(new AccountDeposited(randomAccount.getId(), depositCommand.amount(), expectedBalance, clock.now()));
    }

    @Test
    void shouldDepositAnAccountWithApplyingEvent() {
        final var randomAccount = randomAccount();
        final var depositAccount = randomDeposit(randomAccount.getId());
        final var expectedBalanceAfterDeposit = randomAccount.getBalance().add(depositAccount.amount());

        var events = randomAccount.process(depositAccount, clock).get();
        var updatedAccount = appy(randomAccount, events);

        var balanceAfterDeposit = updatedAccount.getBalance();
        assertThat(events).containsOnly(new AccountDeposited(randomAccount.getId(), depositAccount.amount(), expectedBalanceAfterDeposit, clock.now()));
        assertThat(expectedBalanceAfterDeposit.isEqual(balanceAfterDeposit)).isTrue();
    }

    @Test
    void shouldReturnAccountAlreadyClosed() {
        final var randomAccount = randomAccount();
        final var closeAccount = new CloseAccount(randomAccount.getId());

        var events = randomAccount.process(closeAccount, clock).get();
        var closedAccount = appy(randomAccount, events);

        final var depositAccount = randomDeposit(randomAccount.getId());
        var eventsAfterDeposit = closedAccount.process(depositAccount, clock).getLeft();
        assertThat(eventsAfterDeposit).isEqualTo(AccountCommandError.ACCOUNT_CLOSED);
    }

    @Test
    void shouldWithdrawAnAccount() {
        final var randomAccount = randomAccount();
        final var depositAccount = randomDeposit(randomAccount.getId());
        randomAccount.process(depositAccount, clock);

        final var withdrawAccount = randomWithdraw(randomAccount.getId(), randomAccount.getBalance());
        final var expectedBalance = randomAccount.getBalance().subtract(withdrawAccount.amount());

        var events = randomAccount.process(withdrawAccount, clock).get();
        var accountAfterWithdraw = appy(randomAccount, events);

        assertThat(accountAfterWithdraw.getBalance().isEqual(expectedBalance)).isTrue();
    }

    private Account appy(Account account, List<AccountEvent> events) {
        return events.foldLeft(account, Account::apply);
    }
}