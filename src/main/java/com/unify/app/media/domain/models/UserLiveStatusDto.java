package com.unify.app.media.domain.models;

public record UserLiveStatusDto(
        String id,
        String firstName,
        String lastName,
        String username,
        boolean isLive,
        String avtUrl
) {
}
