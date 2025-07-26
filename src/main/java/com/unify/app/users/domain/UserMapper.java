package com.unify.app.users.domain;

import com.unify.app.users.domain.models.UserDto;
import com.unify.app.users.domain.models.auth.CreateUserCmd;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AvatarMapper.class})
public interface UserMapper {

  @Mapping(source = "avatars", target = "avatar", qualifiedByName = "mapLatestAvatar")
  @Mapping(target = "password", ignore = true)
  UserDto toUserDTO(User user);

  User toUser(UserDto userDto);

  User toUser(CreateUserCmd cmd);

  @Named("mapLatestAvatar")
  default Avatar mapLatestAvatar(Set<Avatar> avatars) {
    return avatars == null || avatars.isEmpty() ? null : avatars.iterator().next();
  }
}
