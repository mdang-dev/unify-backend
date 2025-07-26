package com.unify.app.posts.liked;

import com.unify.app.posts.domain.PostMapper;
import com.unify.app.posts.liked.models.LikedPostDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {PostMapper.class})
public interface LikedPostMapper {

  LikedPostDto toLikedPostDTO(LikedPost likedPost);

  LikedPost toLikedPost(LikedPostDto likedPostDTO);
}
