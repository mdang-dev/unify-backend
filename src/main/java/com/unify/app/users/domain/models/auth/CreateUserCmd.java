package com.unify.app.users.domain.models.auth;

import java.time.LocalDate;

public record CreateUserCmd(
    String firstName,
    String lastName,
    String username,
    String phone,
    String email,
    String password,
    LocalDate birthDay,
    Boolean gender) {}
