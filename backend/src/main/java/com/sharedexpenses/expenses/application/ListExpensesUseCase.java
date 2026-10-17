package com.sharedexpenses.expenses.application;

import com.sharedexpenses.expenses.domain.Expense;
import com.sharedexpenses.groups.application.GroupAccess;

import java.util.List;

public class ListExpensesUseCase {

    private final GroupAccess access;
    private final ExpenseRepository expenses;

    public ListExpensesUseCase(GroupAccess access, ExpenseRepository expenses) {
        this.access = access;
        this.expenses = expenses;
    }

    public List<Expense> execute(Long groupId, Long requesterId) {
        access.requireMember(groupId, requesterId);
        return expenses.findByGroupId(groupId);
    }
}
