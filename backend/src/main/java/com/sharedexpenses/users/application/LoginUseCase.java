package com.sharedexpenses.users.application;

import com.sharedexpenses.shared.domain.UnauthorizedException;
import com.sharedexpenses.users.domain.User;

public class LoginUseCase {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public LoginUseCase(UserRepository users, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    public AuthenticatedUser execute(String email, String password) {
        User user = users.findByEmail(User.normalizeEmail(email))
                .filter(candidate -> password != null && passwordHasher.matches(password, candidate.passwordHash()))
                .orElseThrow(() -> new UnauthorizedException("Email o contraseña incorrectos"));
        return new AuthenticatedUser(tokenIssuer.issue(user), UserSummary.from(user));
    }
}
