package com.sharedexpenses.groups.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @Schema(description = "Código ISO de 3 letras. EUR si se omite", example = "EUR")
        @Pattern(regexp = "[A-Za-z]{3}") String currency) {
}
