package com.sharedexpenses.groups.infrastructure.persistence;

import com.sharedexpenses.groups.application.GroupRepository;
import com.sharedexpenses.groups.domain.Group;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class GroupPersistenceAdapter implements GroupRepository {

    private final GroupJpaRepository jpa;

    public GroupPersistenceAdapter(GroupJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Group save(Group group) {
        return GroupMapper.toDomain(jpa.save(GroupMapper.toEntity(group)));
    }

    @Override
    public Optional<Group> findById(Long id) {
        return jpa.findById(id).map(GroupMapper::toDomain);
    }

    @Override
    public List<Group> findByMember(Long userId) {
        return jpa.findByMember(userId).stream().map(GroupMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
