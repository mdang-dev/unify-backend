package com.unify.app.comments.domain;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

  Comment toComment(CommentDto commentDTO);

  Comment toCommentDto(Comment comment);
}
