package io.artur.bank.base.domain;

import java.time.Instant;

public interface Clock {

    Instant now();

    class SystemClock implements Clock {

        @Override
        public Instant now() {
            return Instant.now();
        }
    }
}
