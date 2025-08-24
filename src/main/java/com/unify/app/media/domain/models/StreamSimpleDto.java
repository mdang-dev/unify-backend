package com.unify.app.media.domain.models;

import com.unify.app.users.domain.models.UserDto;

import java.time.LocalDateTime;

public record StreamSimpleDto(
        String id,
        String name,
        String title,
        String thumbnailUrl,
        Boolean isLive,
        Boolean isChatEnabled,
        Boolean isChatDelayed,
        Boolean isChatFollowersOnly,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
}
