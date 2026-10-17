package com.sharedexpenses.balances.application;

import com.sharedexpenses.balances.domain.DebtSimplifier;
import com.sharedexpenses.balances.domain.SuggestedPayment;

import java.util.List;

public class GetSuggestedSettlementUseCase {

    private final CalculateBalancesUseCase calculateBalances;
    private final DebtSimplifier simplifier;

    public GetSuggestedSettlementUseCase(CalculateBalancesUseCase calculateBalances, DebtSimplifier simplifier) {
        this.calculateBalances = calculateBalances;
        this.simplifier = simplifier;
    }

    public List<SuggestedPayment> execute(Long groupId, Long requesterId) {
        return simplifier.simplify(calculateBalances.execute(groupId, requesterId));
    }
}
