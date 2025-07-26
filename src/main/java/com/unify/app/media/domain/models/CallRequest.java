package com.unify.app.media.domain.models;

import jakarta.validation.constraints.NotBlank;

public record CallRequest(
    @NotBlank String callerId, @NotBlank String calleeId, @NotBlank boolean video) {}
