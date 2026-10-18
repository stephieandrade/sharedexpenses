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

    @Column(precision = 7, scale = 4)
    private BigDecimal percentage;

    protected ExpenseShareEmbeddable() {
    }

    ExpenseShareEmbeddable(Long userId, BigDecimal amount, BigDecimal percentage) {
        this.userId = userId;
        this.amount = amount;
        this.percentage = percentage;
    }

    Long getUserId() {
        return userId;
    }

    BigDecimal getAmount() {
        return amount;
    }

    BigDecimal getPercentage() {
        return percentage;
    }
}
