package com.sharedexpenses.groups.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;

@Embeddable
public class MemberEmbeddable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected MemberEmbeddable() {
    }

    MemberEmbeddable(Long userId, Instant joinedAt) {
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    Long getUserId() {
        return userId;
    }

    Instant getJoinedAt() {
        return joinedAt;
    }
}
