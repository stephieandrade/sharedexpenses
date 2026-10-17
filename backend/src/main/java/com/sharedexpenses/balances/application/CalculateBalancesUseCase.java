package com.sharedexpenses.balances.application;

import com.sharedexpenses.balances.domain.Balance;
import com.sharedexpenses.balances.domain.BalanceCalculator;
import com.sharedexpenses.expenses.application.ListExpensesUseCase;
import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.groups.domain.Group;

import java.util.List;

public class CalculateBalancesUseCase {

    private final GroupAccess access;
    private final ListExpensesUseCase listExpenses;
    private final PaymentRepository payments;
    private final BalanceCalculator calculator;

    public CalculateBalancesUseCase(GroupAccess access, ListExpensesUseCase listExpenses,
                                    PaymentRepository payments, BalanceCalculator calculator) {
        this.access = access;
        this.listExpenses = listExpenses;
        this.payments = payments;
        this.calculator = calculator;
    }

    public List<Balance> execute(Long groupId, Long requesterId) {
        Group group = access.requireMember(groupId, requesterId);
        return calculator.calculate(group.getMemberIds(),
                listExpenses.execute(groupId, requesterId),
                payments.findByGroupId(groupId));
    }
}
