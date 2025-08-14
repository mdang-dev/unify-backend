package com.unify.app.users.domain.models;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.unify.app.users.domain.Role;
import java.time.LocalDate;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record UserDto(
    String id,
    String firstName,
    String lastName,
    String username,
    String phone,
    String email,
    String password,
    Boolean gender,
    LocalDate birthDay,
    String location,
    String education,
    String workAt,
    String biography,
    Integer status,
    Integer reportApprovalCount,
    String currentPassword,
    String newPassword,
    List<Role> roles,
    AvatarDto avatar) {}
