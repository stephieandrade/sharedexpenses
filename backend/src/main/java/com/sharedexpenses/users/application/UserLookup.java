package com.sharedexpenses.users.application;

import com.sharedexpenses.users.domain.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class UserLookup {

    private final UserRepository users;

    public UserLookup(UserRepository users) {
        this.users = users;
    }

    public Optional<UserSummary> findByEmail(String email) {
        return users.findByEmail(User.normalizeEmail(email)).map(UserSummary::from);
    }

    public List<UserSummary> findByIds(Collection<Long> ids) {
        return users.findAllById(ids).stream().map(UserSummary::from).toList();
    }
}
