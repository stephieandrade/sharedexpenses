package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EqualSplitTest {

    private final EqualSplit split = new EqualSplit();

    @Test
    void splitsExactlyWhenThereAreNoLeftoverCents() {
        List<ExpenseShare> shares = split.split(new BigDecimal("30"), participants(1L, 2L, 3L));

        assertThat(amounts(shares)).containsExactly("10.00", "10.00", "10.00");
    }

    @Test
    void leftoverCentsGoToTheFirstParticipants() {
        List<ExpenseShare> shares = split.split(new BigDecimal("10.00"), participants(1L, 2L, 3L));

        assertThat(amounts(shares)).containsExactly("3.34", "3.33", "3.33");
    }

    @Test
    void severalLeftoverCentsAreAssignedOneByOne() {
        List<ExpenseShare> shares = split.split(new BigDecimal("0.05"), participants(1L, 2L, 3L));

        assertThat(amounts(shares)).containsExactly("0.02", "0.02", "0.01");
    }

    @Test
    void resultDoesNotDependOnInputOrder() {
        List<ExpenseShare> shares = split.split(new BigDecimal("10.00"), participants(3L, 1L, 2L));

        assertThat(shares).containsExactly(
                new ExpenseShare(1L, new BigDecimal("3.34")),
                new ExpenseShare(2L, new BigDecimal("3.33")),
                new ExpenseShare(3L, new BigDecimal("3.33")));
    }

    @Test
    void sharesAlwaysAddUpToTheTotal() {
        for (int n = 1; n <= 7; n++) {
            Long[] ids = new Long[n];
            for (int i = 0; i < n; i++) {
                ids[i] = (long) i + 1;
            }
            List<ExpenseShare> shares = split.split(new BigDecimal("100.00"), participants(ids));

            assertThat(sum(shares)).isEqualByComparingTo("100.00");
        }
    }

    @Test
    void rejectsNoParticipants() {
        assertThatThrownBy(() -> split.split(new BigDecimal("10"), List.of()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsDuplicateParticipants() {
        assertThatThrownBy(() -> split.split(new BigDecimal("10"), participants(1L, 1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("repetido");
    }

    @Test
    void rejectsNonPositiveAmountsOrMoreThanTwoDecimals() {
        assertThatThrownBy(() -> split.split(BigDecimal.ZERO, participants(1L)))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> split.split(new BigDecimal("10.001"), participants(1L)))
                .isInstanceOf(BusinessRuleException.class);
    }

    static List<Participant> participants(Long... ids) {
        return Arrays.stream(ids).map(Participant::of).toList();
    }

    static List<String> amounts(List<ExpenseShare> shares) {
        return shares.stream().map(s -> s.amount().toPlainString()).toList();
    }

    static BigDecimal sum(List<ExpenseShare> shares) {
        return shares.stream().map(ExpenseShare::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
