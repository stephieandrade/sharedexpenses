package com.sharedexpenses.users.infrastructure.web;

import com.sharedexpenses.users.application.UserSummary;

public record UserResponse(Long id, String name, String email) {

    static UserResponse from(UserSummary user) {
        return new UserResponse(user.id(), user.name(), user.email());
    }
}
