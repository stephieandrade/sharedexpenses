package com.sharedexpenses.users.domain;

import com.sharedexpenses.shared.domain.Guard;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 72;

    private PasswordPolicy() {
    }

    public static void check(String rawPassword) {
        Guard.check(rawPassword != null
                        && rawPassword.length() >= MIN_LENGTH
                        && rawPassword.length() <= MAX_LENGTH,
                "La contraseña debe tener entre " + MIN_LENGTH + " y " + MAX_LENGTH + " caracteres");
    }
}
