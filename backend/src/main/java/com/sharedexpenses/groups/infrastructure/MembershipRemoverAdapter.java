package com.sharedexpenses.groups.infrastructure;

import com.sharedexpenses.groups.application.LeaveAllGroupsUseCase;
import com.sharedexpenses.users.application.MembershipRemover;

public class MembershipRemoverAdapter implements MembershipRemover {

    private final LeaveAllGroupsUseCase leaveAllGroups;

    public MembershipRemoverAdapter(LeaveAllGroupsUseCase leaveAllGroups) {
        this.leaveAllGroups = leaveAllGroups;
    }

    @Override
    public void removeFromAllGroups(Long userId) {
        leaveAllGroups.execute(userId);
    }
}
