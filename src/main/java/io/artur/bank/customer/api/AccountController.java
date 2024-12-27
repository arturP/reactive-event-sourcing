package io.artur.bank.customer.api;

import io.artur.bank.customer.application.AccountEntityResponse;
import io.artur.bank.customer.application.AccountService;
import io.artur.bank.customer.domain.AccountId;
import io.artur.bank.customer.domain.Money;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletionStage;


import static org.springframework.http.ResponseEntity.accepted;
import static org.springframework.http.ResponseEntity.badRequest;

@RestController
@RequestMapping(value = "/accounts")
class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping(value = "{accountId}", produces = "application/json")
    Mono<AccountResponse> findById(@PathVariable UUID accountId) {
        CompletionStage<AccountResponse> accountResponse = accountService.findAccountBy(AccountId.of(accountId))
                .thenApply(AccountResponse::from);
        return Mono.fromCompletionStage(accountResponse);
    }

    @PatchMapping(value = "{accountId}/deposit", consumes = "application/json")
    Mono<ResponseEntity<String>> deposit(@PathVariable("accountId") UUID accountIdValue,
                                 @RequestBody DepositRequest request) {
        final AccountId accountId = AccountId.of(accountIdValue);
        final Money deposit = Money.of(new BigDecimal(request.amount()));

        CompletionStage<AccountEntityResponse> actionResult = accountService.deposit(accountId, deposit);

        return Mono.fromCompletionStage(actionResult.thenApply(response -> switch (response) {
            case AccountEntityResponse.CommandProcessed ignored -> accepted().body(request.amount() + " deposited");
            case AccountEntityResponse.CommandRejected rejected -> badRequest().body(request.amount() + " deposit failed with: " + rejected.error().name());
        }));
    }
}
