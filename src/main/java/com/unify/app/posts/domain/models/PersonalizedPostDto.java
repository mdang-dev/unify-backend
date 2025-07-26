package com.unify.app.posts.domain.models;

import com.unify.app.posts.domain.Post;

public record PersonalizedPostDto(Post post, Long interactionCount, Long commentCount) {}
