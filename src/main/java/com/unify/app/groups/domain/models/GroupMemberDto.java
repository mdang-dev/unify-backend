package com.unify.app.groups.domain.models;

import java.time.LocalDateTime;

public record GroupMemberDto(
    String id,
    String groupId,
    String userId,
    LocalDateTime joinedAt,
    GroupMemberRole role,
    String username,
    String firstName,
    String lastName,
    String avatarUrl) {}
