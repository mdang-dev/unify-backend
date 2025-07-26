package com.unify.app.media.domain.models;

public record CreateStreamRequest(
    String title, String description, String streamerId, String type) {}
