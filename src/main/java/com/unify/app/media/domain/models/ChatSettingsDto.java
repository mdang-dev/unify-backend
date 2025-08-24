package com.unify.app.media.domain.models;

public record ChatSettingsDto(
        Boolean isChatEnabled,
        Boolean isChatDelayed,
        Boolean isChatFollowersOnly
) {}
