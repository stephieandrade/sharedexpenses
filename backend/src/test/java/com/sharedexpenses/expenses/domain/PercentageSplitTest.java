package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.sharedexpenses.expenses.domain.EqualSplitTest.amounts;
import static com.sharedexpenses.expenses.domain.EqualSplitTest.sum;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PercentageSplitTest {

    private final PercentageSplit split = new PercentageSplit();

    @Test
    void splitsByPercentage() {
        List<ExpenseShare> shares = split.split(new BigDecimal("200.00"), List.of(
                new Participant(1L, new BigDecimal("50")),
                new Participant(2L, new BigDecimal("30")),
                new Participant(3L, new BigDecimal("20"))));

        assertThat(amounts(shares)).containsExactly("100.00", "60.00", "40.00");
    }

    @Test
    void adjustsRoundingCentsDeterministically() {
        List<ExpenseShare> shares = split.split(new BigDecimal("0.10"), List.of(
                new Participant(1L, new BigDecimal("33.33")),
                new Participant(2L, new BigDecimal("33.33")),
                new Participant(3L, new BigDecimal("33.34"))));

        assertThat(amounts(shares)).containsExactly("0.04", "0.03", "0.03");
        assertThat(sum(shares)).isEqualByComparingTo("0.10");
    }

    @Test
    void keepsThePercentageOfEachShare() {
        List<ExpenseShare> shares = split.split(new BigDecimal("10.00"), List.of(
                new Participant(2L, new BigDecimal("33.33")),
                new Participant(1L, new BigDecimal("66.67"))));

        assertThat(shares).extracting(ExpenseShare::percentage)
                .containsExactly(new BigDecimal("66.67"), new BigDecimal("33.33"));
    }

    @Test
    void rejectsPercentagesWithMoreThanFourDecimals() {
        assertThatThrownBy(() -> split.split(new BigDecimal("10"), List.of(
                new Participant(1L, new BigDecimal("33.33333")),
                new Participant(2L, new BigDecimal("66.66667")))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("decimales");
    }

    @Test
    void rejectsPercentagesThatDoNotAddUpTo100() {
        assertThatThrownBy(() -> split.split(new BigDecimal("10"), List.of(
                new Participant(1L, new BigDecimal("50")),
                new Participant(2L, new BigDecimal("40")))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("100");
    }

    @Test
    void rejectsMissingOrZeroPercentages() {
        assertThatThrownBy(() -> split.split(new BigDecimal("10"), List.of(new Participant(1L, null))))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> split.split(new BigDecimal("10"), List.of(
                new Participant(1L, new BigDecimal("100")),
                new Participant(2L, BigDecimal.ZERO))))
                .isInstanceOf(BusinessRuleException.class);
    }
}
