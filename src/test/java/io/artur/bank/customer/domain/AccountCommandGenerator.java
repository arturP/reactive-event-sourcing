package io.artur.bank.customer.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import io.artur.bank.customer.domain.AccountCommand.Deposit;
import io.artur.bank.customer.domain.AccountCommand.Withdraw;


public class AccountCommandGenerator {

    private static final Random random = new Random();

    public static Deposit randomDeposit(AccountId accountId) {
        return new AccountCommand.Deposit(accountId, generateRandomMoney());
    }

    public static Withdraw randomWithdraw(AccountId accountId, Money balance) {
        return new Withdraw(accountId, generateRandomMoneyLessThen(balance));
    }

    public static Money generateRandomMoney() {
        double randomValue = 1000 * random.nextDouble(); // Generates a random value between 0 and 1000
        return Money.of(BigDecimal.valueOf(randomValue).setScale(2, RoundingMode.HALF_UP)); // Rounds to 2 decimal places
    }

    public static Money generateRandomMoneyLessThen(Money base) {
        double randomValue = base.value().doubleValue() * random.nextDouble(); // Generates a random value between 0 and base
        return Money.of(BigDecimal.valueOf(randomValue).setScale(2, RoundingMode.HALF_UP)); // Rounds to 2 decimal places
    }
}
