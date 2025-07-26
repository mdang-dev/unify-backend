package com.unify.app.posts.domain.models;

public record MediaDto(String id, String url, String fileType, Long size, MediaType mediaType) {}
