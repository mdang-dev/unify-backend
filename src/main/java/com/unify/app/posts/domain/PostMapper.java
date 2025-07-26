package com.unify.app.posts.domain;

import com.unify.app.posts.domain.models.PostDto;
import com.unify.app.users.domain.AvatarMapper;
import com.unify.app.users.domain.UserMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class, AvatarMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

  Post toPost(PostDto postDTO);

  PostDto toPostDto(Post post);

  List<Post> toPostList(List<PostDto> dtoList);

  List<PostDto> toPostDtoList(List<Post> posts);
}
