package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.Amounts;
import com.sharedexpenses.shared.domain.Guard;

import java.math.BigDecimal;

public record ExpenseShare(Long userId, BigDecimal amount) {

    public ExpenseShare {
        Guard.required(userId, "El usuario de la parte es obligatorio");
        amount = Amounts.normalize(amount);
        Guard.check(amount.signum() >= 0, "Una parte no puede ser negativa");
    }
}
