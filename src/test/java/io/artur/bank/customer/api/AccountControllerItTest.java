package io.artur.bank.customer.api;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AccountControllerItTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void shouldGetAccountById() {
        //given
        final UUID accountId = UUID.randomUUID();

        //when //then
        webClient.get().uri("/accounts/{accountId}", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountResponse.class)
                .value(shouldHaveId(accountId));
    }

    private BaseMatcher<AccountResponse> shouldHaveId(UUID accountId) {
        return new BaseMatcher<>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof AccountResponse accountResponse) {
                    return UUID.fromString(accountResponse.id()).equals(accountId);
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("AccountResponse should have id: " + accountId);
            }
        };
    }
}