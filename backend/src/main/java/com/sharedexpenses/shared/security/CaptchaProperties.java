package com.sharedexpenses.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "app.captcha")
public record CaptchaProperties(String secret, URI verifyUrl) {

    public boolean enabled() {
        return secret != null && !secret.isBlank();
    }
}
