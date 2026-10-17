package com.sharedexpenses.users.infrastructure.persistence;

import com.sharedexpenses.users.application.UserRepository;
import com.sharedexpenses.users.domain.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    public UserPersistenceAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public User save(User user) {
        return UserMapper.toDomain(jpa.save(UserMapper.toEntity(user)));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(UserMapper::toDomain);
    }

    @Override
    public List<User> findAllById(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(UserMapper::toDomain).toList();
    }
}
