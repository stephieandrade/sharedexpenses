package com.sharedexpenses.groups.infrastructure.web;

import com.sharedexpenses.groups.domain.Group;

import java.time.Instant;

public record GroupSummaryResponse(Long id, String name, String description, String currency, Long creatorId,
                                   Instant createdAt, int memberCount) {

    static GroupSummaryResponse from(Group group) {
        return new GroupSummaryResponse(group.getId(), group.getName(), group.getDescription(), group.getCurrency(),
                group.getCreatorId(), group.getCreatedAt(), group.getMembers().size());
    }
}
