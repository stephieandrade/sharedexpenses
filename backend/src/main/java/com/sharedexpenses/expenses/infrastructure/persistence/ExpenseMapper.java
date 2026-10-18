package com.sharedexpenses.expenses.infrastructure.persistence;

import com.sharedexpenses.expenses.domain.Expense;
import com.sharedexpenses.expenses.domain.ExpenseShare;

final class ExpenseMapper {

    private ExpenseMapper() {
    }

    static ExpenseEntity toEntity(Expense expense) {
        return new ExpenseEntity(expense.id(), expense.groupId(), expense.description(), expense.amount(),
                expense.paidById(), expense.date(), expense.splitType(),
                expense.shares().stream().map(s -> new ExpenseShareEmbeddable(s.userId(), s.amount(), s.percentage())).toList(),
                expense.createdById());
    }

    static Expense toDomain(ExpenseEntity entity) {
        return new Expense(entity.getId(), entity.getGroupId(), entity.getDescription(), entity.getAmount(),
                entity.getPaidById(), entity.getDate(), entity.getSplitType(),
                entity.getShares().stream().map(s -> new ExpenseShare(s.getUserId(), s.getAmount(), s.getPercentage())).toList(),
                entity.getCreatedById());
    }
}
