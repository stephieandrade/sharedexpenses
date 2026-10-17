package com.sharedexpenses.groups.infrastructure;

import com.sharedexpenses.groups.application.AddMemberUseCase;
import com.sharedexpenses.groups.application.CreateGroupUseCase;
import com.sharedexpenses.groups.application.GetGroupDetailUseCase;
import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.groups.application.GroupRepository;
import com.sharedexpenses.groups.application.ListMyGroupsUseCase;
import com.sharedexpenses.groups.application.MemberBalanceProvider;
import com.sharedexpenses.groups.application.RemoveMemberUseCase;
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
}
