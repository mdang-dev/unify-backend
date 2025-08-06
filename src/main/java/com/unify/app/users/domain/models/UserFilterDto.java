package com.unify.app.users.domain.models;

import java.time.LocalDate;

public record UserFilterDto(
    LocalDate birthDay,
    String email,
    Integer status,
    String username,
    String firstName,
    String lastName) {}
