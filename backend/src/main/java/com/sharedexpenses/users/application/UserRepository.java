package com.sharedexpenses.users.application;

import com.sharedexpenses.users.domain.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    List<User> findAllById(Collection<Long> ids);
}
