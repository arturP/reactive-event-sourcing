package io.artur.bank.customer.domain;

import io.artur.bank.base.domain.Clock;

import java.time.Instant;

public class FixedClock implements Clock {

        private final Instant now;

        public FixedClock(Instant instant) {
            this.now = instant;
        }

        @Override
        public Instant now() {
            return now;
        }
}
