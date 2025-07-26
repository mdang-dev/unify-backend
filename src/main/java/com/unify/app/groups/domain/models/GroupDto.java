package com.unify.app.groups.domain.models;

import java.time.LocalDateTime;
import java.util.List;

public record GroupDto(
    String id,
    String name,
    PrivacyType privacyType,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String coverImageUrl,
    GroupStatus status,
    List<String> memberIds) {}
