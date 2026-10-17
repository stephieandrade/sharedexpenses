package com.sharedexpenses.groups.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddMemberRequest(@NotBlank @Email String email) {
}
