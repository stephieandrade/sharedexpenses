package com.sharedexpenses.groups.infrastructure;

import com.sharedexpenses.groups.application.AddMemberUseCase;
import com.sharedexpenses.groups.application.CreateGroupUseCase;
import com.sharedexpenses.groups.application.DeleteGroupUseCase;
import com.sharedexpenses.groups.application.GetGroupDetailUseCase;
import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.groups.application.GroupRepository;
import com.sharedexpenses.groups.application.LeaveAllGroupsUseCase;
import com.sharedexpenses.groups.application.ListMyGroupsUseCase;
import com.sharedexpenses.groups.application.MemberBalanceProvider;
import com.sharedexpenses.groups.application.RemoveMemberUseCase;
import com.sharedexpenses.groups.application.UpdateGroupUseCase;
import com.sharedexpenses.users.application.MembershipRemover;
import com.sharedexpenses.users.application.UserLookup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class GroupsConfig {

    @Bean
    GroupAccess groupAccess(GroupRepository groups) {
        return new GroupAccess(groups);
    }

    @Bean
    CreateGroupUseCase createGroupUseCase(GroupRepository groups, Clock clock) {
        return new CreateGroupUseCase(groups, clock);
    }

    @Bean
    ListMyGroupsUseCase listMyGroupsUseCase(GroupRepository groups) {
        return new ListMyGroupsUseCase(groups);
    }

    @Bean
    GetGroupDetailUseCase getGroupDetailUseCase(GroupAccess access, UserLookup users) {
        return new GetGroupDetailUseCase(access, users);
    }

    @Bean
    AddMemberUseCase addMemberUseCase(GroupAccess access, GroupRepository groups, UserLookup users, Clock clock) {
        return new AddMemberUseCase(access, groups, users, clock);
    }

    @Bean
    RemoveMemberUseCase removeMemberUseCase(GroupAccess access, GroupRepository groups, MemberBalanceProvider balances) {
        return new RemoveMemberUseCase(access, groups, balances);
    }

    @Bean
    UpdateGroupUseCase updateGroupUseCase(GroupAccess access, GroupRepository groups) {
        return new UpdateGroupUseCase(access, groups);
    }

    @Bean
    DeleteGroupUseCase deleteGroupUseCase(GroupAccess access, GroupRepository groups) {
        return new DeleteGroupUseCase(access, groups);
    }

    @Bean
    LeaveAllGroupsUseCase leaveAllGroupsUseCase(GroupRepository groups, MemberBalanceProvider balances) {
        return new LeaveAllGroupsUseCase(groups, balances);
    }

    @Bean
    MembershipRemover membershipRemover(LeaveAllGroupsUseCase leaveAllGroups) {
        return new MembershipRemoverAdapter(leaveAllGroups);
    }
}
