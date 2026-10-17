package com.sharedexpenses.expenses.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class ExpenseShareEmbeddable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected ExpenseShareEmbeddable() {
    }

    ExpenseShareEmbeddable(Long userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
    }

    Long getUserId() {
        return userId;
    }

    BigDecimal getAmount() {
        return amount;
    }
}
