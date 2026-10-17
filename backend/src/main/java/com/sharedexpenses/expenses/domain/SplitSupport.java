package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.Amounts;
import com.sharedexpenses.shared.domain.Guard;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class SplitSupport {

    private SplitSupport() {
    }

    static List<Participant> sortedParticipants(List<Participant> participants) {
        Guard.check(participants != null && !participants.isEmpty(),
                "El gasto debe tener al menos un participante");
        Set<Long> seen = new HashSet<>();
        for (Participant p : participants) {
            Guard.check(seen.add(p.userId()), "Participante repetido: " + p.userId());
        }
        return participants.stream().sorted(Comparator.comparing(Participant::userId)).toList();
    }

    static List<ExpenseShare> withLeftoverCents(BigDecimal total, List<Participant> participants,
                                                List<BigDecimal> base) {
        int leftoverCents = total.subtract(Amounts.sum(base)).movePointRight(Amounts.DECIMALS).intValueExact();
        List<ExpenseShare> shares = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            BigDecimal amount = i < leftoverCents ? base.get(i).add(Amounts.CENT) : base.get(i);
            shares.add(new ExpenseShare(participants.get(i).userId(), amount));
        }
        return shares;
    }
}
