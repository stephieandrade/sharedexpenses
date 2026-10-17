package com.sharedexpenses.expenses.infrastructure.web;

import com.sharedexpenses.expenses.domain.ExpenseShare;

import java.math.BigDecimal;

public record ExpenseShareResponse(Long userId, BigDecimal amount) {

    static ExpenseShareResponse from(ExpenseShare share) {
        return new ExpenseShareResponse(share.userId(), share.amount());
    }
}
