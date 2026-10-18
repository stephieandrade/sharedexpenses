package com.sharedexpenses.users.application;

import com.sharedexpenses.shared.domain.BusinessRuleException;
import com.sharedexpenses.shared.domain.Guard;
import com.sharedexpenses.shared.domain.NotFoundException;
import com.sharedexpenses.users.domain.PasswordPolicy;
import com.sharedexpenses.users.domain.User;

public class ChangePasswordUseCase {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public ChangePasswordUseCase(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    public void execute(Long userId, String currentPassword, String newPassword) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (currentPassword == null || !passwordHasher.matches(currentPassword, user.passwordHash())) {
            throw new BusinessRuleException("La contraseña actual no es correcta");
        }
        PasswordPolicy.check(newPassword);
        Guard.check(!newPassword.equals(currentPassword), "La nueva contraseña debe ser distinta de la actual");
        users.save(user.withPasswordHash(passwordHasher.hash(newPassword)));
    }
}
