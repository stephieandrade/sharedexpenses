package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.shared.domain.NotFoundException;
import com.sharedexpenses.users.application.UserLookup;
import com.sharedexpenses.users.application.UserSummary;

import java.time.Clock;

public class AddMemberUseCase {

    private final GroupAccess access;
    private final GroupRepository groups;
    private final UserLookup users;
    private final Clock clock;

    public AddMemberUseCase(GroupAccess access, GroupRepository groups, UserLookup users, Clock clock) {
        this.access = access;
        this.groups = groups;
        this.users = users;
        this.clock = clock;
    }

    public UserSummary execute(Long groupId, Long requesterId, String email) {
        Group group = access.requireMember(groupId, requesterId);
        UserSummary user = users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No existe ningún usuario con el email " + email));
        group.addMember(user.id(), clock.instant());
        groups.save(group);
        return user;
    }
}
