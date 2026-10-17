package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.Amounts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

public class EqualSplit implements SplitStrategy {

    @Override
    public SplitType type() {
        return SplitType.EQUAL;
    }

    @Override
    public List<ExpenseShare> split(BigDecimal amount, List<Participant> participants) {
        BigDecimal total = Amounts.positive(amount);
        List<Participant> sorted = SplitSupport.sortedParticipants(participants);
        BigDecimal base = total.divide(BigDecimal.valueOf(sorted.size()), Amounts.DECIMALS, RoundingMode.DOWN);
        return SplitSupport.withLeftoverCents(total, sorted, Collections.nCopies(sorted.size(), base));
    }
}
