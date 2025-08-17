package com.unify.app.users.domain.models.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record CreateUserCmd(
    @NotBlank(message = "First name is required") String firstName,
    @NotBlank(message = "Last name is required") String lastName,
    @NotBlank(message = "Username is required")
        @Pattern(
            regexp = "^[a-zA-Z0-9_]{3,20}$",
            message =
                "Username must be 3-20 characters long and contain only letters, numbers, and underscores")
        String username,
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
    @NotBlank(message = "Password is required")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$",
            message =
                "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number")
        String password,
    @NotNull(message = "Gender is required") Boolean gender,
    @NotNull(message = "Birthday is required") @Past(message = "Birthday must be in the past")
        LocalDate birthDay) {}
