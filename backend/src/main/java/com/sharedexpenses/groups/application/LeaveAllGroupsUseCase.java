package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.shared.domain.Amounts;
import com.sharedexpenses.shared.domain.Guard;

import java.util.List;

public class LeaveAllGroupsUseCase {

    private final GroupRepository groups;
    private final MemberBalanceProvider balances;

    public LeaveAllGroupsUseCase(GroupRepository groups, MemberBalanceProvider balances) {
        this.groups = groups;
        this.balances = balances;
    }

    public void execute(Long userId) {
        List<Group> memberships = groups.findByMember(userId);
        for (Group group : memberships) {
            if (group.isCreator(userId)) {
                Guard.check(group.getMembers().size() == 1, "Eres el creador de \"" + group.getName()
                        + "\" y tiene otros miembros. Elimina el grupo antes de borrar tu cuenta");
            } else {
                Guard.check(balances.balanceOf(group.getId(), userId).signum() == 0, "Tienes un balance pendiente en \""
                        + group.getName() + "\". Salda tus deudas antes de borrar tu cuenta");
            }
        }
        for (Group group : memberships) {
            if (group.isCreator(userId)) {
                groups.deleteById(group.getId());
            } else {
                group.removeMember(userId, userId, Amounts.ZERO);
                groups.save(group);
            }
        }
    }
}
