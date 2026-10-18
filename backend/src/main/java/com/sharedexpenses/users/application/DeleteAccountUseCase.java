package com.sharedexpenses.users.application;

import com.sharedexpenses.shared.domain.BusinessRuleException;
import com.sharedexpenses.shared.domain.NotFoundException;
import com.sharedexpenses.users.domain.User;

import java.util.UUID;

public class DeleteAccountUseCase {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final MembershipRemover membershipRemover;

    public DeleteAccountUseCase(UserRepository users, PasswordHasher passwordHasher,
                                MembershipRemover membershipRemover) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.membershipRemover = membershipRemover;
    }

    public void execute(Long userId, String password) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (password == null || !passwordHasher.matches(password, user.passwordHash())) {
            throw new BusinessRuleException("La contraseña no es correcta");
        }
        membershipRemover.removeFromAllGroups(userId);
        users.save(user.anonymized(passwordHasher.hash(UUID.randomUUID().toString())));
    }
}
