package com.sharedexpenses.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(Limit registerPerClient, Limit registerTotal, Limit loginPerClient) {

    public record Limit(int maxRequests, Duration window) {
    }
}
