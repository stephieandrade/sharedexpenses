package com.sharedexpenses.expenses.application;

import com.sharedexpenses.expenses.domain.Expense;
import com.sharedexpenses.expenses.domain.Participant;
import com.sharedexpenses.expenses.domain.SplitStrategyRegistry;
import com.sharedexpenses.expenses.domain.SplitType;
import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.shared.domain.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class UpdateExpenseUseCase {

    public record Command(Long groupId, Long expenseId, Long requesterId, String description, BigDecimal amount,
                          Long paidById, LocalDate date, SplitType splitType, List<Participant> participants) {
    }

    private final GroupAccess access;
    private final ExpenseRepository expenses;
    private final SplitStrategyRegistry strategies;

    public UpdateExpenseUseCase(GroupAccess access, ExpenseRepository expenses, SplitStrategyRegistry strategies) {
        this.access = access;
        this.expenses = expenses;
        this.strategies = strategies;
    }

    public Expense execute(Command command) {
        Group group = access.requireMember(command.groupId(), command.requesterId());
        Expense current = expenses.findById(command.expenseId())
                .filter(expense -> expense.groupId().equals(command.groupId()))
                .orElseThrow(() -> new NotFoundException("Gasto no encontrado"));
        LocalDate date = command.date() != null ? command.date() : current.date();

        Expense updated = current.update(command.description(), command.amount(), command.paidById(), date,
                strategies.forType(command.splitType()), command.participants());
        group.requireMembers(updated.involvedUsers());

        return expenses.save(updated);
    }
}
