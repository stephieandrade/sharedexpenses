package com.sharedexpenses.expenses.infrastructure.web;

import com.sharedexpenses.expenses.domain.Expense;
import com.sharedexpenses.expenses.domain.SplitType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseResponse(Long id, String description, BigDecimal amount, Long paidById, LocalDate date,
                              SplitType splitType, List<ExpenseShareResponse> shares) {

    static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(expense.id(), expense.description(), expense.amount(), expense.paidById(),
                expense.date(), expense.splitType(),
                expense.shares().stream().map(ExpenseShareResponse::from).toList());
    }
}
