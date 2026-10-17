package com.sharedexpenses.expenses.application;

import com.sharedexpenses.expenses.domain.AmountSplit;
import com.sharedexpenses.expenses.domain.EqualSplit;
import com.sharedexpenses.expenses.domain.Expense;
import com.sharedexpenses.expenses.domain.ExpenseShare;
import com.sharedexpenses.expenses.domain.Participant;
import com.sharedexpenses.expenses.domain.PercentageSplit;
import com.sharedexpenses.expenses.domain.SplitStrategyRegistry;
import com.sharedexpenses.expenses.domain.SplitType;
import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.groups.application.GroupRepository;
import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.shared.domain.BusinessRuleException;
import com.sharedexpenses.shared.domain.ForbiddenException;
import com.sharedexpenses.shared.domain.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseUseCasesTest {

    private static final Instant NOW = Instant.parse("2026-10-17T10:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 17);

    @Mock
    GroupRepository groups;
    @Mock
    ExpenseRepository expenses;

    CreateExpenseUseCase createExpense;
    GroupAccess access;

    @BeforeEach
    void setUp() {
        Group group = new Group(10L, "Piso", null, "EUR", 1L, NOW, List.of(), 0L);
        group.addMember(1L, NOW);
        group.addMember(2L, NOW);
        when(groups.findById(10L)).thenReturn(Optional.of(group));

        access = new GroupAccess(groups);
        SplitStrategyRegistry strategies = new SplitStrategyRegistry(List.of(new EqualSplit(), new AmountSplit(), new PercentageSplit()));
        createExpense = new CreateExpenseUseCase(access, expenses, strategies, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsAnExpenseWithTheChosenStrategyAndTodayAsDefaultDate() {
        when(expenses.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Expense expense = createExpense.execute(command(1L, 1L, SplitType.PERCENTAGES, List.of(
                new Participant(1L, new BigDecimal("75")), new Participant(2L, new BigDecimal("25")))));

        assertThat(expense.date()).isEqualTo(TODAY);
        assertThat(expense.splitType()).isEqualTo(SplitType.PERCENTAGES);
        assertThat(expense.shares()).containsExactly(
                new ExpenseShare(1L, new BigDecimal("30.00")), new ExpenseShare(2L, new BigDecimal("10.00")));
    }

    @Test
    void rejectsParticipantsWhoAreNotMembers() {
        assertThatThrownBy(() -> createExpense.execute(command(1L, 1L, SplitType.EQUAL,
                List.of(Participant.of(1L), Participant.of(5L)))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("5");
        verify(expenses, never()).save(any());
    }

    @Test
    void rejectsAPayerWhoIsNotAMember() {
        assertThatThrownBy(() -> createExpense.execute(command(1L, 8L, SplitType.EQUAL, List.of(Participant.of(1L)))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void nonMembersCannotCreateExpenses() {
        assertThatThrownBy(() -> createExpense.execute(command(99L, 1L, SplitType.EQUAL, List.of(Participant.of(1L)))))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void listsTheGroupExpenses() {
        Expense expense = sampleExpense(10L);
        when(expenses.findByGroupId(10L)).thenReturn(List.of(expense));

        assertThat(new ListExpensesUseCase(access, expenses).execute(10L, 2L)).containsExactly(expense);
    }

    @Test
    void deletesAnExpenseOfTheGroup() {
        when(expenses.findById(50L)).thenReturn(Optional.of(sampleExpense(10L)));

        new DeleteExpenseUseCase(access, expenses).execute(10L, 50L, 1L);

        verify(expenses).deleteById(50L);
    }

    @Test
    void cannotDeleteAnExpenseFromAnotherGroup() {
        when(expenses.findById(50L)).thenReturn(Optional.of(sampleExpense(77L)));

        assertThatThrownBy(() -> new DeleteExpenseUseCase(access, expenses).execute(10L, 50L, 1L))
                .isInstanceOf(NotFoundException.class);
        verify(expenses, never()).deleteById(any());
    }

    private static CreateExpenseUseCase.Command command(Long requesterId, Long paidById, SplitType type,
                                                        List<Participant> participants) {
        return new CreateExpenseUseCase.Command(10L, requesterId, "Compra", new BigDecimal("40.00"), paidById,
                null, type, participants);
    }

    private static Expense sampleExpense(Long groupId) {
        return new Expense(50L, groupId, "Cena", new BigDecimal("20.00"), 1L, TODAY, SplitType.EQUAL,
                List.of(new ExpenseShare(1L, new BigDecimal("10.00")), new ExpenseShare(2L, new BigDecimal("10.00"))));
    }
}
