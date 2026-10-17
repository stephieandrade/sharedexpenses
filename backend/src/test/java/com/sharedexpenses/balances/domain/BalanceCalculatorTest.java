package com.sharedexpenses.balances.domain;

import com.sharedexpenses.expenses.domain.EqualSplit;
import com.sharedexpenses.expenses.domain.Expense;
import com.sharedexpenses.expenses.domain.Participant;
import com.sharedexpenses.expenses.domain.PercentageSplit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 17);
    private final BalanceCalculator calculator = new BalanceCalculator();

    @Test
    void payerBecomesCreditorAndOthersDebtors() {
        Expense dinner = equalExpense(new BigDecimal("30.00"), 1L, 1L, 2L, 3L);

        List<Balance> balances = calculator.calculate(List.of(1L, 2L, 3L), List.of(dinner), List.of());

        assertThat(balances).containsExactly(
                new Balance(1L, new BigDecimal("20.00")),
                new Balance(2L, new BigDecimal("-10.00")),
                new Balance(3L, new BigDecimal("-10.00")));
    }

    @Test
    void paymentsSettleTheDebt() {
        Expense dinner = equalExpense(new BigDecimal("30.00"), 1L, 1L, 2L, 3L);
        Payment payment = new Payment(null, 1L, 2L, 1L, new BigDecimal("10.00"), TODAY);

        List<Balance> balances = calculator.calculate(List.of(1L, 2L, 3L), List.of(dinner), List.of(payment));

        assertThat(balances).containsExactly(
                new Balance(1L, new BigDecimal("10.00")),
                new Balance(2L, new BigDecimal("0.00")),
                new Balance(3L, new BigDecimal("-10.00")));
    }

    @Test
    void hidesFormerMembersWhoseAccountsAreSettled() {
        Expense dinner = equalExpense(new BigDecimal("20.00"), 1L, 1L, 4L);
        Payment settled = new Payment(null, 1L, 4L, 1L, new BigDecimal("10.00"), TODAY);

        List<Balance> balances = calculator.calculate(List.of(1L, 2L), List.of(dinner), List.of(settled));

        assertThat(balances).extracting(Balance::userId).containsExactly(1L, 2L);
    }

    @Test
    void keepsFormerMembersWithPendingBalance() {
        Expense dinner = equalExpense(new BigDecimal("20.00"), 1L, 1L, 4L);

        List<Balance> balances = calculator.calculate(List.of(1L), List.of(dinner), List.of());

        assertThat(balances).containsExactly(
                new Balance(1L, new BigDecimal("10.00")),
                new Balance(4L, new BigDecimal("-10.00")));
    }

    @Test
    void includesMembersWithoutMovementsWithZeroBalance() {
        List<Balance> balances = calculator.calculate(List.of(1L, 2L), List.of(), List.of());

        assertThat(balances).extracting(Balance::amount)
                .allSatisfy(amount -> assertThat(amount).isEqualByComparingTo("0"));
    }

    @Test
    void balancesAlwaysAddUpToZero() {
        List<Expense> expenses = List.of(
                equalExpense(new BigDecimal("10.00"), 1L, 1L, 2L, 3L),
                equalExpense(new BigDecimal("47.21"), 2L, 1L, 2L, 3L, 4L),
                Expense.create(1L, "Hotel", new BigDecimal("333.33"), 4L, TODAY, new PercentageSplit(), List.of(
                        new Participant(1L, new BigDecimal("12.5")),
                        new Participant(3L, new BigDecimal("37.5")),
                        new Participant(4L, new BigDecimal("50")))));
        List<Payment> payments = List.of(new Payment(null, 1L, 3L, 4L, new BigDecimal("15.55"), TODAY));

        List<Balance> balances = calculator.calculate(List.of(1L, 2L, 3L, 4L), expenses, payments);

        BigDecimal total = balances.stream().map(Balance::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(total).isEqualByComparingTo("0");
    }

    private static Expense equalExpense(BigDecimal amount, Long payer, Long... participants) {
        List<Participant> list = Arrays.stream(participants).map(Participant::of).toList();
        return Expense.create(1L, "Gasto", amount, payer, TODAY, new EqualSplit(), list);
    }
}
