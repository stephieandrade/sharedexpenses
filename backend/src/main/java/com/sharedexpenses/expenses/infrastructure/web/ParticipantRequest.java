package com.sharedexpenses.expenses.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ParticipantRequest(
        @NotNull Long userId,
        @Schema(description = "Importe (AMOUNTS) o porcentaje (PERCENTAGES). Se ignora en EQUAL", example = "50")
        BigDecimal value) {
}
