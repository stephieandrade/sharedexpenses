package com.sharedexpenses.expenses.infrastructure.web;

import com.sharedexpenses.expenses.domain.ExpenseShare;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ExpenseShareResponse(
        Long userId,
        BigDecimal amount,
        @Schema(description = "Porcentaje indicado al crear el gasto (solo en PERCENTAGES)") BigDecimal percentage) {

    static ExpenseShareResponse from(ExpenseShare share) {
        return new ExpenseShareResponse(share.userId(), share.amount(), share.percentage());
    }
}
