package com.unify.app.media.domain.models;

import jakarta.validation.constraints.NotBlank;

public record StreamUpdateDto(
         String title,
         String thumbnailUrl,
         String description
) {
}
