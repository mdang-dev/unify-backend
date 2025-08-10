package com.unify.app.comments.domain;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

  Comment toComment(CommentDto commentDTO);

  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "post.id", target = "postId")
  @Mapping(source = "user.username", target = "username")
  CommentDto toCommentDTO(Comment comment);
}
