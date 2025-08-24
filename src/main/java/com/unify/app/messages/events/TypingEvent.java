package com.unify.app.messages.events;

public record TypingEvent(String fromUser, String toUser, boolean typing, String timestamp) {}
