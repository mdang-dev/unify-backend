package com.unify.app.posts.liked.models;

import jakarta.validation.constraints.NotBlank;

public record LikedPostRequest(@NotBlank String userId, @NotBlank String postId) {}
