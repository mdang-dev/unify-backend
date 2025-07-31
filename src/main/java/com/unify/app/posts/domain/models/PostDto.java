package com.unify.app.posts.domain.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.unify.app.hashtags.domain.models.HashtagDetailDto;
import com.unify.app.users.domain.models.UserDto;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostDto {

  String id;

  String captions;

  // 0 -> hidden
  // 1 -> visible
  // 2 -> sensitive/violent content
  Integer status = 1;

  Audience audience;

  UserDto user;

  LocalDateTime postedAt;

  Boolean isCommentVisible;

  Boolean isLikeVisible = false;

  Set<MediaDto> media;

  Set<HashtagDetailDto> hashtags;

  Long commentCount;
}
