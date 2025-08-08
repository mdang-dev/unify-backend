package com.unify.app.media.domain.models;

public record DebugUrlDto(
    String type, String host, String wsUrl, String apiKey, String apiSecret) {}
