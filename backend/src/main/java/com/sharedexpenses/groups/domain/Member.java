package com.sharedexpenses.groups.domain;

import com.sharedexpenses.shared.domain.Guard;

import java.time.Instant;

public record Member(Long userId, Instant joinedAt) {

    public Member {
        Guard.required(userId, "El usuario del miembro es obligatorio");
        Guard.required(joinedAt, "La fecha de alta es obligatoria");
    }
}
