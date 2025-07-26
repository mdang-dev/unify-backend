package com.unify.app.posts.saved;

import com.unify.app.posts.domain.PostMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = {PostMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SavedPostMapper {

  @Mapping(target = "user", ignore = true)
  @Mapping(target = "post", ignore = true)
  SavedPost toSavedPost(SavedPostDto savedPostDTO);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "post", source = "post")
  SavedPostDto toSavedPostDTO(SavedPost savedPost);
}
