package com.unify.app.users.domain;

import com.unify.app.users.domain.models.AvatarDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvatarMapper {

  AvatarDto toAvatarDTO(Avatar avatar);

  @Mapping(target = "user", ignore = true)
  Avatar toAvatar(AvatarDto avatarDTO);
}
