package com.sharedexpenses.balances.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterPaymentRequest(
        @NotNull Long fromUserId,
        @NotNull Long toUserId,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @Schema(description = "Hoy si se omite") LocalDate date) {
}
