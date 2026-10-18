package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.shared.domain.ForbiddenException;

public class DeleteGroupUseCase {

    private final GroupAccess access;
    private final GroupRepository groups;

    public DeleteGroupUseCase(GroupAccess access, GroupRepository groups) {
        this.access = access;
        this.groups = groups;
    }

    public void execute(Long groupId, Long requesterId) {
        Group group = access.requireMember(groupId, requesterId);
        if (!group.isCreator(requesterId)) {
            throw new ForbiddenException("Solo el creador del grupo puede eliminarlo");
        }
        groups.deleteById(groupId);
    }
}
