package com.unify.app.users.domain.models;

import java.time.LocalDateTime;

public record AvatarDto(String id, String url, LocalDateTime createdAt) {}
