package io.artur.bank.customer.domain;

import java.time.Instant;
import java.util.Random;

public class DomainGenerator {

    private static Random random = new Random();

    public static AccountId generateAccountId() {
        return AccountId.of();
    }

    public static AccountType generateAccountType() {
        return AccountType.values()[random.nextInt(AccountType.values().length)];
    }

    public static String generateName() {
        return "Account-Name" + random.nextInt(1000);
    }

    public static Account randomAccount() {
        return Account.create(generateAccountId(), generateName(), generateAccountType(), new FixedClock(Instant.now()));
    }
}
