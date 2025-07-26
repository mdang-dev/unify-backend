package com.unify.app.users.domain.models.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email,
    @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String newPassword) {}
