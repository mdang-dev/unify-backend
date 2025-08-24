package com.unify.app.users.domain;

import com.unify.app.media.domain.StreamMapper;
import com.unify.app.users.domain.models.UserDto;
import com.unify.app.users.domain.models.UserWithStreamDto;
import com.unify.app.users.domain.models.auth.CreateUserCmd;

import java.util.Comparator;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AvatarMapper.class, StreamMapper.class})
public interface UserMapper {

  @Mapping(source = "avatars", target = "avatar", qualifiedByName = "mapLatestAvatar")
  @Mapping(target = "password", ignore = true)
  UserDto toUserDTO(User user);

  User toUser(UserDto userDto);

  User toUser(CreateUserCmd cmd);
  @Mapping(source = "avatars", target = "avatar", qualifiedByName = "mapLatestAvatar")
  UserWithStreamDto toWithStreamDto(User user);

    @Named("mapLatestAvatar")
    default Avatar mapLatestAvatar(Set<Avatar> avatars) {
        if (avatars == null || avatars.isEmpty()) {
            return null;
        }

        return avatars.stream()
                .max(Comparator.comparing(Avatar::getCreatedAt))
                .orElse(null);
    }
    }
