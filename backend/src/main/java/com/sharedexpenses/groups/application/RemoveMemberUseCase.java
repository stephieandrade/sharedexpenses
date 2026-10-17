package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.shared.domain.NotFoundException;

public class RemoveMemberUseCase {

    private final GroupAccess access;
    private final GroupRepository groups;
    private final MemberBalanceProvider balances;

    public RemoveMemberUseCase(GroupAccess access, GroupRepository groups, MemberBalanceProvider balances) {
        this.access = access;
        this.groups = groups;
        this.balances = balances;
    }

    public void execute(Long groupId, Long requesterId, Long userId) {
        Group group = access.requireMember(groupId, requesterId);
        if (!group.isMember(userId)) {
            throw new NotFoundException("El usuario no es miembro del grupo");
        }
        group.checkCanRemove(userId, requesterId);
        group.removeMember(userId, requesterId, balances.balanceOf(groupId, userId));
        groups.save(group);
    }
}
