package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.Amounts;
import com.sharedexpenses.shared.domain.Guard;

import java.math.BigDecimal;
import java.util.List;

public class AmountSplit implements SplitStrategy {

    @Override
    public SplitType type() {
        return SplitType.AMOUNTS;
    }

    @Override
    public List<ExpenseShare> split(BigDecimal amount, List<Participant> participants) {
        BigDecimal total = Amounts.positive(amount);
        List<ExpenseShare> shares = SplitSupport.sortedParticipants(participants).stream()
                .map(p -> new ExpenseShare(p.userId(), Amounts.positive(p.value())))
                .toList();
        BigDecimal sum = Amounts.sum(shares.stream().map(ExpenseShare::amount).toList());
        Guard.check(sum.compareTo(total) == 0,
                "Las partes suman " + sum + " pero el gasto es de " + total);
        return shares;
    }
}
