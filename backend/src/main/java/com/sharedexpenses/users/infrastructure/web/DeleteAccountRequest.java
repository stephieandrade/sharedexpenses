package com.sharedexpenses.users.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(@NotBlank String password) {
}
