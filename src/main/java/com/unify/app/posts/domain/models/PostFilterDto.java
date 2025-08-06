package com.unify.app.posts.domain.models;

import com.unify.app.hashtags.domain.models.HashtagDetailDto;
import java.time.LocalDateTime;
import java.util.Set;

public record PostFilterDto(
    String captions,
    Integer status,
    Audience audience,
    LocalDateTime postedAt,
    Boolean isCommentVisible,
    Boolean isLikeVisible,
    Set<HashtagDetailDto> hashtags,
    Long commentCount,
    String commentCountOperator // ">", "<", "=", ">=", "<="
    ) {}
