package com.unify.app.hashtags.domain.models;

import com.unify.app.posts.domain.models.PostDto;

public record HashtagDetailDto(String id, PostDto post, HashtagDto hashtag) {}
