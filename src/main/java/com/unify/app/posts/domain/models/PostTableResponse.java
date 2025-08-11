package com.unify.app.posts.domain.models;

import java.util.List;

public record PostTableResponse(List<PostRowDto> rows, int page, int pageSize, long total) {}
