package com.sharedexpenses.expenses.infrastructure.web;

import com.sharedexpenses.expenses.domain.SplitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseRequest(
        @NotBlank @Size(max = 200) String description,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull Long paidById,
        @Schema(description = "Hoy si se omite") LocalDate date,
        @NotNull SplitType splitType,
        @NotEmpty List<@Valid @NotNull ParticipantRequest> participants) {
}
