package com.sharedexpenses.balances.domain;

import com.sharedexpenses.shared.domain.Amounts;
import com.sharedexpenses.shared.domain.Guard;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DebtSimplifier {

    public List<SuggestedPayment> simplify(List<Balance> balances) {
        Map<Long, BigDecimal> pending = new TreeMap<>();
        for (Balance b : balances) {
            if (b.amount().signum() != 0) {
                pending.put(b.userId(), b.amount());
            }
        }
        Guard.check(Amounts.sum(pending.values()).signum() == 0, "Los balances del grupo deben sumar 0");

        List<SuggestedPayment> payments = new ArrayList<>();
        while (!pending.isEmpty()) {
            Long creditor = Collections.max(pending.entrySet(), Map.Entry.comparingByValue()).getKey();
            Long debtor = Collections.min(pending.entrySet(), Map.Entry.comparingByValue()).getKey();
            BigDecimal amount = pending.get(creditor).min(pending.get(debtor).negate());

            payments.add(new SuggestedPayment(debtor, creditor, amount));
            update(pending, creditor, amount.negate());
            update(pending, debtor, amount);
        }
        return payments;
    }

    private static void update(Map<Long, BigDecimal> pending, Long userId, BigDecimal amount) {
        BigDecimal updated = pending.get(userId).add(amount);
        if (updated.signum() == 0) {
            pending.remove(userId);
        } else {
            pending.put(userId, updated);
        }
    }
}
