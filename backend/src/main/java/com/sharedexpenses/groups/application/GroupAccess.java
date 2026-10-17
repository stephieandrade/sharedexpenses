package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.shared.domain.ForbiddenException;
import com.sharedexpenses.shared.domain.NotFoundException;

public class GroupAccess {

    private final GroupRepository groups;

    public GroupAccess(GroupRepository groups) {
        this.groups = groups;
    }

    public Group requireMember(Long groupId, Long userId) {
        Group group = groups.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo no encontrado"));
        if (!group.isMember(userId)) {
            throw new ForbiddenException("No eres miembro de este grupo");
        }
        return group;
    }
}
