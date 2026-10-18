package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;

import java.util.List;
import java.util.Optional;

public interface GroupRepository {

    Group save(Group group);

    Optional<Group> findById(Long id);

    List<Group> findByMember(Long userId);

    void deleteById(Long id);
}
