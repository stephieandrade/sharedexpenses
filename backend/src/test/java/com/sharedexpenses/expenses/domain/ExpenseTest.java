package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.sharedexpenses.expenses.domain.EqualSplitTest.participants;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpenseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 17);

    @Test
    void createsTheExpenseWithTheStrategyShares() {
        Expense expense = Expense.create(1L, "  Cena  ", new BigDecimal("10"), 1L, TODAY,
                new EqualSplit(), participants(1L, 2L, 3L));

        assertThat(expense.description()).isEqualTo("Cena");
        assertThat(expense.amount()).isEqualByComparingTo("10.00");
        assertThat(expense.splitType()).isEqualTo(SplitType.EQUAL);
        assertThat(expense.shares()).hasSize(3);
    }

    @Test
    void involvedUsersIncludesThePayerEvenIfNotParticipating() {
        Expense expense = Expense.create(1L, "Regalo", new BigDecimal("20"), 9L, TODAY,
                new EqualSplit(), participants(1L, 2L));

        assertThat(expense.involvedUsers()).containsExactlyInAnyOrder(1L, 2L, 9L);
    }

    @Test
    void rejectsSharesThatDoNotAddUpToTheAmount() {
        assertThatThrownBy(() -> new Expense(null, 1L, "Cena", new BigDecimal("10.00"), 1L, TODAY, SplitType.AMOUNTS,
                List.of(new ExpenseShare(1L, new BigDecimal("5.00")), new ExpenseShare(2L, new BigDecimal("4.99")))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsBlankDescription() {
        assertThatThrownBy(() -> Expense.create(1L, " ", new BigDecimal("10"), 1L, TODAY,
                new EqualSplit(), participants(1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("descripción");
    }
}
