package com.sharedexpenses.users.application;

import com.sharedexpenses.users.domain.User;

public record UserSummary(Long id, String name, String email) {

    public static UserSummary from(User user) {
        return new UserSummary(user.id(), user.name(), user.email());
    }
}
