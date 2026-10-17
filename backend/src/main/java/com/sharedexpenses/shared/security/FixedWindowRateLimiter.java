package com.sharedexpenses.shared.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FixedWindowRateLimiter {

    private static final int MAX_TRACKED_KEYS = 10_000;

    private record Window(Instant start, int requests) {
    }

    private final int maxRequests;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int maxRequests, Duration window, Clock clock) {
        this.maxRequests = maxRequests;
        this.window = window;
        this.clock = clock;
    }

    public Optional<Duration> tryAcquire(String key) {
        Instant now = clock.instant();
        if (windows.size() > MAX_TRACKED_KEYS) {
            windows.values().removeIf(w -> isExpired(w, now));
        }
        Window current = windows.compute(key, (k, existing) -> existing == null || isExpired(existing, now)
                ? new Window(now, 1)
                : new Window(existing.start(), existing.requests() + 1));

        if (current.requests() <= maxRequests) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(now, current.start().plus(window)));
    }

    private boolean isExpired(Window w, Instant now) {
        return !now.isBefore(w.start().plus(window));
    }
}
