package com.unify.app.comments.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CommentDto(
    String id,
    String content,
    String userId,
    String postId,
    String username,
    String avatarUrl,
    String parentId,
    Integer status,
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            timezone = "UTC")
        LocalDateTime commentedAt,
    List<CommentDto> replies)
    implements Serializable {

  public CommentDto(Comment comment) {
    this(
        comment.getId(),
        comment.getContent(),
        comment.getUser().getId(),
        comment.getPost().getId(),
        comment.getUser().getUsername(),
        comment.getUser().latestAvatar() != null ? comment.getUser().latestAvatar().getUrl() : null,
        comment.getParent() != null ? comment.getParent().getId() : null,
        comment.getStatus(),
        comment.getCommentedAt(),
        comment.getReplies() == null
            ? List.of()
            : comment.getReplies().stream().map(CommentDto::new).collect(Collectors.toList()));
  }
}
