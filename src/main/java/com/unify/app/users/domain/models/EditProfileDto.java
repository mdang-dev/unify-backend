package com.unify.app.users.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public record EditProfileDto(
    String firstName,
    String lastName,
    String phone,
    String email,
    Boolean gender,
    LocalDate birthDay,
    String location,
    String education,
    String workAt,
    String biography,
    AvatarDto avatar) {}
