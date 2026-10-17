package com.sharedexpenses.groups.infrastructure.persistence;

import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.groups.domain.Member;

final class GroupMapper {

    private GroupMapper() {
    }

    static GroupEntity toEntity(Group group) {
        return new GroupEntity(group.getId(), group.getName(), group.getDescription(), group.getCurrency(),
                group.getCreatorId(), group.getCreatedAt(),
                group.getMembers().stream().map(m -> new MemberEmbeddable(m.userId(), m.joinedAt())).toList(),
                group.getVersion());
    }

    static Group toDomain(GroupEntity entity) {
        return new Group(entity.getId(), entity.getName(), entity.getDescription(), entity.getCurrency(),
                entity.getCreatorId(), entity.getCreatedAt(),
                entity.getMembers().stream().map(m -> new Member(m.getUserId(), m.getJoinedAt())).toList(),
                entity.getVersion());
    }
}
