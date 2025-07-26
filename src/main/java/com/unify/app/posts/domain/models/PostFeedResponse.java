package com.unify.app.posts.domain.models;

import java.util.List;

public record PostFeedResponse(List<PostDto> posts, boolean hasNextPage, int currentPage) {}
