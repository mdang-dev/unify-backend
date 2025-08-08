package com.unify.app.media.domain.models;

public record CallTokenResponse(
    String token, boolean video, boolean isCaller, String calleeName, String calleeAvatar) {}
