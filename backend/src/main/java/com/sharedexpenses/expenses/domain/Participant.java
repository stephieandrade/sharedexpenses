package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.Guard;

import java.math.BigDecimal;

public record Participant(Long userId, BigDecimal value) {

    public Participant {
        Guard.required(userId, "El usuario participante es obligatorio");
    }

    public static Participant of(Long userId) {
        return new Participant(userId, null);
    }
}
