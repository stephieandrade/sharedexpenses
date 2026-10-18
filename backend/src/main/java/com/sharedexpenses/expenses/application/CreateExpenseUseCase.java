package com.sharedexpenses.expenses.application;

import com.sharedexpenses.expenses.domain.Expense;
import com.sharedexpenses.expenses.domain.Participant;
import com.sharedexpenses.expenses.domain.SplitStrategyRegistry;
import com.sharedexpenses.expenses.domain.SplitType;
import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.groups.domain.Group;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class CreateExpenseUseCase {

    public record Command(Long groupId, Long requesterId, String description, BigDecimal amount, Long paidById,
                          LocalDate date, SplitType splitType, List<Participant> participants) {
    }

    private final GroupAccess access;
    private final ExpenseRepository expenses;
    private final SplitStrategyRegistry strategies;
    private final Clock clock;

    public CreateExpenseUseCase(GroupAccess access, ExpenseRepository expenses, SplitStrategyRegistry strategies,
                                Clock clock) {
        this.access = access;
        this.expenses = expenses;
        this.strategies = strategies;
        this.clock = clock;
    }

    public Expense execute(Command command) {
        Group group = access.requireMember(command.groupId(), command.requesterId());
        LocalDate date = command.date() != null ? command.date() : LocalDate.now(clock);

        Expense expense = Expense.create(command.groupId(), command.description(), command.amount(),
                command.paidById(), date, strategies.forType(command.splitType()), command.participants())
                .createdBy(command.requesterId());
        group.requireMembers(expense.involvedUsers());

        return expenses.save(expense);
    }
}
