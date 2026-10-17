package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.sharedexpenses.expenses.domain.EqualSplitTest.amounts;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmountSplitTest {

    private final AmountSplit split = new AmountSplit();

    @Test
    void usesTheGivenAmounts() {
        List<ExpenseShare> shares = split.split(new BigDecimal("50.00"), List.of(
                new Participant(2L, new BigDecimal("30")),
                new Participant(1L, new BigDecimal("20.00"))));

        assertThat(shares).extracting(ExpenseShare::userId).containsExactly(1L, 2L);
        assertThat(amounts(shares)).containsExactly("20.00", "30.00");
    }

    @Test
    void rejectsAmountsThatDoNotAddUpToTheTotal() {
        assertThatThrownBy(() -> split.split(new BigDecimal("50.00"), List.of(
                new Participant(1L, new BigDecimal("20.00")),
                new Participant(2L, new BigDecimal("29.99")))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("49.99");
    }

    @Test
    void rejectsMissingOrNonPositiveAmounts() {
        assertThatThrownBy(() -> split.split(new BigDecimal("10"), List.of(new Participant(1L, null))))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> split.split(new BigDecimal("10"), List.of(
                new Participant(1L, new BigDecimal("10")),
                new Participant(2L, BigDecimal.ZERO))))
                .isInstanceOf(BusinessRuleException.class);
    }
}
