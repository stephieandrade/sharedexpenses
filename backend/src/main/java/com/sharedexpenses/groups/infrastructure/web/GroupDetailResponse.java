package com.sharedexpenses.groups.infrastructure.web;

import com.sharedexpenses.groups.application.GroupDetail;
import com.sharedexpenses.groups.domain.Group;

import java.time.Instant;
import java.util.List;

public record GroupDetailResponse(Long id, String name, String description, String currency, Long creatorId,
                                  Instant createdAt, List<MemberResponse> members) {

    static GroupDetailResponse from(GroupDetail detail) {
        Group group = detail.group();
        return new GroupDetailResponse(group.getId(), group.getName(), group.getDescription(), group.getCurrency(),
                group.getCreatorId(), group.getCreatedAt(),
                detail.members().stream().map(MemberResponse::from).toList());
    }
}
