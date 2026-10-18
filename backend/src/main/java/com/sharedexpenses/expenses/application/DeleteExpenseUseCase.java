package com.sharedexpenses.expenses.application;

import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.shared.domain.NotFoundException;

public class DeleteExpenseUseCase {

    private final GroupAccess access;
    private final ExpenseRepository expenses;
    private final GroupPaymentsRemover paymentsRemover;

    public DeleteExpenseUseCase(GroupAccess access, ExpenseRepository expenses, GroupPaymentsRemover paymentsRemover) {
        this.access = access;
        this.expenses = expenses;
        this.paymentsRemover = paymentsRemover;
    }

    public void execute(Long groupId, Long expenseId, Long requesterId) {
        access.requireMember(groupId, requesterId);
        expenses.findById(expenseId)
                .filter(expense -> expense.groupId().equals(groupId))
                .orElseThrow(() -> new NotFoundException("Gasto no encontrado"));
        expenses.deleteById(expenseId);
        if (expenses.findByGroupId(groupId).isEmpty()) {
            paymentsRemover.removeAllPayments(groupId);
        }
    }
}
