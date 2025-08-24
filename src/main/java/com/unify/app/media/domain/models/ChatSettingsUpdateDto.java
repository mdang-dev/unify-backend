package com.unify.app.media.domain.models;

public record ChatSettingsUpdateDto(
        String type,
        ChatSettingsDto settings
) {}
