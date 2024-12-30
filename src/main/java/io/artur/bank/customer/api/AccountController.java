package io.artur.bank.customer.api;

import io.artur.bank.customer.application.AccountEntityResponse;
import io.artur.bank.customer.application.AccountService;
import io.artur.bank.customer.domain.AccountId;
import io.artur.bank.customer.domain.Money;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping(value = "/accounts")
class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public Mono<ResponseEntity<String>> create(@RequestBody CreateAccountRequest request) {
        final String name = request.name();
        final String type = request.type();
        final AccountId accountId = AccountId.of(request.accountId());

        CompletionStage<ResponseEntity<String>> accountCreateResponse = accountService.createAccount(accountId, name, type)
                .thenApply(response -> switch (response) {
            case AccountEntityResponse.CommandProcessed ignored -> new ResponseEntity<>("Account created", HttpStatus.CREATED);
            case AccountEntityResponse.CommandRejected rejected -> transformFromRejection(rejected);
        });
        return Mono.fromCompletionStage(accountCreateResponse);
    }

    @GetMapping(value = "{accountId}", produces = "application/json")
    Mono<ResponseEntity<AccountResponse>> findById(@PathVariable UUID accountId) {
        CompletionStage<ResponseEntity<AccountResponse>> accountResponse = accountService.findAccountBy(AccountId.of(accountId))
                .thenApply(result -> result.map(AccountResponse::from).map(ok()::body)
                        .getOrElse(notFound().build()));

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

    private ResponseEntity<String> transformFromRejection(AccountEntityResponse.CommandRejected rejected) {
        return switch (rejected.error()) {
            case ACCOUNT_ALREADY_EXISTS -> new ResponseEntity<>("Account already created", HttpStatus.CONFLICT);
            default -> badRequest().body("Account creation failed with: " + rejected.error().name());
        };
    }
}
