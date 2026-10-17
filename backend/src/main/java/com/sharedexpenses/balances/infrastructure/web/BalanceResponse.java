package com.sharedexpenses.balances.infrastructure.web;

import com.sharedexpenses.balances.domain.Balance;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record BalanceResponse(
        Long userId,
        @Schema(description = "Positivo: le deben dinero. Negativo: debe dinero") BigDecimal amount) {

    static BalanceResponse from(Balance balance) {
        return new BalanceResponse(balance.userId(), balance.amount());
    }
}
