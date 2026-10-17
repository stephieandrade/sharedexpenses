package com.sharedexpenses.balances.domain;

import com.sharedexpenses.shared.domain.Amounts;
import com.sharedexpenses.shared.domain.Guard;

import java.math.BigDecimal;

public record Balance(Long userId, BigDecimal amount) {

    public Balance {
        Guard.required(userId, "El usuario del balance es obligatorio");
        amount = Amounts.normalize(amount);
    }
}
