package com.sharedexpenses.groups.infrastructure.web;

import com.sharedexpenses.users.application.UserSummary;

public record MemberResponse(Long id, String name, String email) {

    static MemberResponse from(UserSummary user) {
        return new MemberResponse(user.id(), user.name(), user.email());
    }
}
