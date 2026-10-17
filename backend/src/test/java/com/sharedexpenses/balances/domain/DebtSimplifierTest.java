package com.sharedexpenses.balances.domain;

import com.sharedexpenses.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebtSimplifierTest {

    private final DebtSimplifier simplifier = new DebtSimplifier();

    @Test
    void biggestDebtorPaysBiggestCreditor() {
        List<SuggestedPayment> payments = simplifier.simplify(List.of(
                balance(1L, "20.00"), balance(2L, "-10.00"), balance(3L, "-10.00")));

        assertThat(payments).containsExactly(
                new SuggestedPayment(2L, 1L, new BigDecimal("10.00")),
                new SuggestedPayment(3L, 1L, new BigDecimal("10.00")));
    }

    @Test
    void chainsPaymentsWhenOneDebtorOwesSeveralCreditors() {
        List<SuggestedPayment> payments = simplifier.simplify(List.of(
                balance(1L, "30.00"), balance(2L, "10.00"), balance(3L, "-40.00")));

        assertThat(payments).containsExactly(
                new SuggestedPayment(3L, 1L, new BigDecimal("30.00")),
                new SuggestedPayment(3L, 2L, new BigDecimal("10.00")));
    }

    @Test
    void noDebtsMeansNoPayments() {
        assertThat(simplifier.simplify(List.of(balance(1L, "0.00"), balance(2L, "0")))).isEmpty();
    }

    @Test
    void rejectsBalancesThatDoNotAddUpToZero() {
        assertThatThrownBy(() -> simplifier.simplify(List.of(balance(1L, "10.00"), balance(2L, "-9.99"))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void producesAtMostNMinusOnePaymentsThatSettleAllDebts() {
        Random random = new Random(42);
        for (int round = 0; round < 200; round++) {
            List<Balance> balances = randomBalances(random, 2 + random.nextInt(9));

            List<SuggestedPayment> payments = simplifier.simplify(balances);

            assertThat(payments).hasSizeLessThanOrEqualTo(balances.size() - 1);
            assertThat(payments).allSatisfy(p -> assertThat(p.amount().signum()).isPositive());
            assertThat(apply(balances, payments).values())
                    .allSatisfy(total -> assertThat(total).isEqualByComparingTo("0"));
        }
    }

    private static List<Balance> randomBalances(Random random, int n) {
        List<Balance> balances = new ArrayList<>();
        BigDecimal accumulated = BigDecimal.ZERO;
        for (long id = 1; id < n; id++) {
            BigDecimal amount = BigDecimal.valueOf(random.nextInt(20001) - 10000, 2);
            balances.add(new Balance(id, amount));
            accumulated = accumulated.add(amount);
        }
        balances.add(new Balance((long) n, accumulated.negate()));
        return balances;
    }

    private static Map<Long, BigDecimal> apply(List<Balance> balances, List<SuggestedPayment> payments) {
        Map<Long, BigDecimal> totals = new HashMap<>();
        balances.forEach(b -> totals.put(b.userId(), b.amount()));
        for (SuggestedPayment p : payments) {
            totals.merge(p.fromUserId(), p.amount(), BigDecimal::add);
            totals.merge(p.toUserId(), p.amount().negate(), BigDecimal::add);
        }
        return totals;
    }

    private static Balance balance(Long userId, String amount) {
        return new Balance(userId, new BigDecimal(amount));
    }
}
