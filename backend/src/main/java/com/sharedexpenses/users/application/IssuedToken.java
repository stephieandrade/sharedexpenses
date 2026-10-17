package com.sharedexpenses.users.application;

import java.time.Instant;

public record IssuedToken(String value, Instant expiresAt) {
}
