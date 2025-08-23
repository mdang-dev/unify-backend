package com.unify.app.media.domain.models;

public record StreamChatSettingsDto(
        Boolean isChatEnabled,
        Boolean isChatDelayed,
        Boolean isChatFollowersOnly
) {}
