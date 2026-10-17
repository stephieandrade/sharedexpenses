package com.sharedexpenses.shared.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class FixedWindowRateLimiterTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-17T10:00:00Z"));
    private final FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(3, Duration.ofMinutes(5), clock);

    @Test
    void allowsUpToTheLimitWithinTheWindow() {
        assertThat(limiter.tryAcquire("ip-1")).isEmpty();
        assertThat(limiter.tryAcquire("ip-1")).isEmpty();
        assertThat(limiter.tryAcquire("ip-1")).isEmpty();

        clock.advance(Duration.ofMinutes(2));

        assertThat(limiter.tryAcquire("ip-1")).contains(Duration.ofMinutes(3));
    }

    @Test
    void startsAgainWhenTheWindowEnds() {
        for (int i = 0; i < 4; i++) {
            limiter.tryAcquire("ip-1");
        }

        clock.advance(Duration.ofMinutes(5));

        assertThat(limiter.tryAcquire("ip-1")).isEmpty();
    }

    @Test
    void countsEachKeySeparately() {
        for (int i = 0; i < 3; i++) {
            limiter.tryAcquire("ip-1");
        }

        assertThat(limiter.tryAcquire("ip-1")).isPresent();
        assertThat(limiter.tryAcquire("ip-2")).isEmpty();
    }

    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
