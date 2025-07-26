package com.unify.app.followers.domain.models;

import com.unify.app.users.domain.models.UserDto;
import java.time.LocalDateTime;

public record FollowerDto(
    FollowerUserId id, UserDto userFollower, UserDto userFollowing, LocalDateTime createAt) {}
