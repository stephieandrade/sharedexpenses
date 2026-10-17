package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;

import java.util.List;

public class ListMyGroupsUseCase {

    private final GroupRepository groups;

    public ListMyGroupsUseCase(GroupRepository groups) {
        this.groups = groups;
    }

    public List<Group> execute(Long userId) {
        return groups.findByMember(userId);
    }
}
