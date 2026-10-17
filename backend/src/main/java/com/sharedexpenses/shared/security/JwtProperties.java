package com.sharedexpenses.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration, boolean allowRandomSecret) {

    public static final String ISSUER = "shared-expenses";
}
