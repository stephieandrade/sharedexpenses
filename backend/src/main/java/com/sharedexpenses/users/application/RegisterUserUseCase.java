package com.sharedexpenses.users.application;

import com.sharedexpenses.shared.domain.ConflictException;
import com.sharedexpenses.shared.domain.Guard;
import com.sharedexpenses.users.domain.User;

public class RegisterUserUseCase {

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 72;

    public record Command(String name, String email, String password) {
    }

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public RegisterUserUseCase(UserRepository users, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    public AuthenticatedUser execute(Command command) {
        String password = command.password();
        Guard.check(password != null
                        && password.length() >= MIN_PASSWORD_LENGTH
                        && password.length() <= MAX_PASSWORD_LENGTH,
                "La contraseña debe tener entre 8 y 72 caracteres");
        String email = User.normalizeEmail(command.email());
        if (users.findByEmail(email).isPresent()) {
            throw new ConflictException("Ya existe una cuenta con el email " + email);
        }

        User user = users.save(new User(null, command.name(), email, passwordHasher.hash(password)));
        return new AuthenticatedUser(tokenIssuer.issue(user), UserSummary.from(user));
    }
}
