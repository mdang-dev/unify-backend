package com.unify.app.media.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CallTokenResponse(
        String token,
        boolean video,
        boolean isCaller,
        String calleeName,
        String calleeAvatar) {
}
