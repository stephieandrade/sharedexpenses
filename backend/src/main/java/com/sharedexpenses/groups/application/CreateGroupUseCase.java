package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;

import java.time.Clock;

public class CreateGroupUseCase {

    public record Command(String name, String description, String currency, Long creatorId) {
    }

    private final GroupRepository groups;
    private final Clock clock;

    public CreateGroupUseCase(GroupRepository groups, Clock clock) {
        this.groups = groups;
        this.clock = clock;
    }

    public Group execute(Command command) {
        Group group = Group.create(command.name(), command.description(), command.currency(),
                command.creatorId(), clock.instant());
        return groups.save(group);
    }
}
