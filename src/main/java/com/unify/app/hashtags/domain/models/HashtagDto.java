package com.unify.app.hashtags.domain.models;

import java.util.List;

public record HashtagDto(String id, String content, List<HashtagDetailDto> hashtags) {}
