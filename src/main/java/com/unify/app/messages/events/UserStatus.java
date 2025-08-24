package com.unify.app.messages.events;

import java.time.LocalDateTime;

public record UserStatus(String userId, boolean active, LocalDateTime lastActive) {}
