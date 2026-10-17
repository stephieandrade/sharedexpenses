package com.sharedexpenses.balances.infrastructure;

import com.sharedexpenses.balances.application.CalculateBalancesUseCase;
import com.sharedexpenses.balances.domain.Balance;
import com.sharedexpenses.groups.application.MemberBalanceProvider;
import com.sharedexpenses.shared.domain.Amounts;

import java.math.BigDecimal;

public class MemberBalanceAdapter implements MemberBalanceProvider {

    private final CalculateBalancesUseCase calculateBalances;

    public MemberBalanceAdapter(CalculateBalancesUseCase calculateBalances) {
        this.calculateBalances = calculateBalances;
    }

    @Override
    public BigDecimal balanceOf(Long groupId, Long userId) {
        return calculateBalances.execute(groupId, userId).stream()
                .filter(balance -> balance.userId().equals(userId))
                .map(Balance::amount)
                .findFirst()
                .orElse(Amounts.ZERO);
    }
}
