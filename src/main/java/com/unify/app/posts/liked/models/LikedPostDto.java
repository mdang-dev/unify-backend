package com.unify.app.posts.liked.models;

import com.unify.app.posts.domain.models.PostDto;
import com.unify.app.users.domain.models.UserDto;

public record LikedPostDto(String id, PostDto post, UserDto user) {}
