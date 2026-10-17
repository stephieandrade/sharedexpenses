package com.sharedexpenses.users.domain;

import com.sharedexpenses.shared.domain.Guard;

import java.util.Locale;

public record User(Long id, String name, String email, String passwordHash) {

    public User {
        name = Guard.requiredText(name, "El nombre es obligatorio");
        email = normalizeEmail(email);
        Guard.check(email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"), "El email no es válido");
        Guard.required(passwordHash, "La contraseña es obligatoria");
    }

    public static String normalizeEmail(String email) {
        return Guard.requiredText(email, "El email es obligatorio").toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", name=" + name + ", email=" + email + "]";
    }
}
