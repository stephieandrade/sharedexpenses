package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;

public class UpdateGroupUseCase {

    public record Command(Long groupId, Long requesterId, String name, String description) {
    }

    private final GroupAccess access;
    private final GroupRepository groups;

    public UpdateGroupUseCase(GroupAccess access, GroupRepository groups) {
        this.access = access;
        this.groups = groups;
    }

    public Group execute(Command command) {
        Group group = access.requireMember(command.groupId(), command.requesterId());
        return groups.save(group.withDetails(command.name(), command.description()));
    }
}
