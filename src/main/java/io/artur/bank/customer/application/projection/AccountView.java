package io.artur.bank.customer.application.projection;

import java.math.BigDecimal;

public record AccountView(String accountId, BigDecimal balance) {
}
