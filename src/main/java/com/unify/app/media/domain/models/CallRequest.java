package com.unify.app.media.domain.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CallRequest(
    @NotBlank String callerId, @NotBlank String calleeId, @NotNull boolean video) {}
