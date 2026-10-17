package com.sharedexpenses.expenses.application;

import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.shared.domain.NotFoundException;

public class DeleteExpenseUseCase {

    private final GroupAccess access;
    private final ExpenseRepository expenses;

    public DeleteExpenseUseCase(GroupAccess access, ExpenseRepository expenses) {
        this.access = access;
        this.expenses = expenses;
    }

    public void execute(Long groupId, Long expenseId, Long requesterId) {
        access.requireMember(groupId, requesterId);
        expenses.findById(expenseId)
                .filter(expense -> expense.groupId().equals(groupId))
                .orElseThrow(() -> new NotFoundException("Gasto no encontrado"));
        expenses.deleteById(expenseId);
    }
}
