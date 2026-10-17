package com.sharedexpenses.balances.domain;

import com.sharedexpenses.expenses.domain.Expense;
import com.sharedexpenses.expenses.domain.ExpenseShare;
import com.sharedexpenses.shared.domain.Amounts;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BalanceCalculator {

    public List<Balance> calculate(Collection<Long> members, List<Expense> expenses, List<Payment> payments) {
        Map<Long, BigDecimal> totals = new TreeMap<>();
        members.forEach(id -> totals.put(id, Amounts.ZERO));

        for (Expense expense : expenses) {
            add(totals, expense.paidById(), expense.amount());
            for (ExpenseShare share : expense.shares()) {
                add(totals, share.userId(), share.amount().negate());
            }
        }
        for (Payment payment : payments) {
            add(totals, payment.fromUserId(), payment.amount());
            add(totals, payment.toUserId(), payment.amount().negate());
        }

        return totals.entrySet().stream()
                .filter(e -> members.contains(e.getKey()) || e.getValue().signum() != 0)
                .map(e -> new Balance(e.getKey(), e.getValue()))
                .toList();
    }

    private static void add(Map<Long, BigDecimal> totals, Long userId, BigDecimal amount) {
        totals.merge(userId, amount, BigDecimal::add);
    }
}
