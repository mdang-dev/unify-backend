package com.unify.app.posts.domain.models;

import java.time.LocalDateTime;

public record PostRowDto(
    int no, // running index in current page (start at 1)
    String user, // Display Name or @handle
    String captions, // post captions
    int status, // 0|1|2
    String audience, // PUBLIC, PRIVATE
    LocalDateTime postedAt, // when post was created
    long comments, // comment count
    PostActionDto actions // include postId for FE actions (edit/view/delete)
    ) {
  public record PostActionDto(String postId) {}
}
