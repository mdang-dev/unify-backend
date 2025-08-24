package com.unify.app.users.domain.models;

import com.unify.app.media.domain.models.StreamSimpleDto;
import com.unify.app.users.domain.Role;

import java.time.LocalDate;
import java.util.List;

public record UserWithStreamDto(
        String id,
        String firstName,
        String lastName,
        String username,
        AvatarDto avatar,
        StreamSimpleDto stream
) {
}
