package com.unify.app.posts.domain.models;

public record PersonalizedPostResponseDto(
    PostDto post,
    Long interactionCount,
    Long commentCount,
    String postType // 'SELF', 'FOLLOWED', or 'OTHER'
    ) {}
