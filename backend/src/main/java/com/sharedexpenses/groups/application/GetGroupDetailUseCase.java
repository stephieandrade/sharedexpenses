package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.groups.domain.Member;
import com.sharedexpenses.users.application.UserLookup;
import com.sharedexpenses.users.application.UserSummary;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GetGroupDetailUseCase {

    private final GroupAccess access;
    private final UserLookup users;

    public GetGroupDetailUseCase(GroupAccess access, UserLookup users) {
        this.access = access;
        this.users = users;
    }

    public GroupDetail execute(Long groupId, Long requesterId) {
        Group group = access.requireMember(groupId, requesterId);
        Map<Long, UserSummary> byId = users.findByIds(group.getMemberIds()).stream()
                .collect(Collectors.toMap(UserSummary::id, Function.identity()));
        List<UserSummary> members = group.getMembers().stream()
                .map(Member::userId)
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();
        return new GroupDetail(group, members);
    }
}
