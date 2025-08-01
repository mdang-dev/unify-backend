package com.unify.app.users.domain.models.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    @NotBlank(message = "Email is required!") @Email(message = "Invalid email format!")
        String email) {}
