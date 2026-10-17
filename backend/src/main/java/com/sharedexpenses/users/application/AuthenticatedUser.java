package com.sharedexpenses.users.application;

public record AuthenticatedUser(IssuedToken token, UserSummary user) {
}
