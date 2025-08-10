package com.unify.app.posts.domain.models;

import com.unify.app.hashtags.domain.models.HashtagDetailDto;
import java.util.Set;

public record PostFilterDto(
    String captions,
    Integer status,
    Audience audience,
    Boolean isCommentVisible,
    Boolean isLikeVisible,
    Set<HashtagDetailDto> hashtags,
    Long commentCount,
    String commentCountOperator // ">", "<", "=", ">=", "<="
    ) {}
