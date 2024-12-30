package io.artur.bank.customer.api;

import java.util.UUID;

public record CreateAccountRequest(UUID accountId, String name, String type) {
}
