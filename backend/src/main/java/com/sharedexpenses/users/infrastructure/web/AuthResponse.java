package com.sharedexpenses.users.infrastructure.web;

import com.sharedexpenses.users.application.AuthenticatedUser;

import java.time.Instant;

public record AuthResponse(String token, Instant expiresAt, UserResponse user) {

    static AuthResponse from(AuthenticatedUser authenticated) {
        return new AuthResponse(authenticated.token().value(), authenticated.token().expiresAt(),
                UserResponse.from(authenticated.user()));
    }
}
