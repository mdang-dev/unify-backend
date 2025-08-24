package com.unify.app.media.domain.models;

import com.unify.app.users.domain.models.UserDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class StreamDto{
    String id;
    String name;
    String title;
    String description;
    String thumbnailUrl;
    UserDto user;
    Boolean isLive;
    Boolean isChatEnabled;
    Boolean isChatDelayed;
    Boolean isChatFollowersOnly;
    LocalDateTime createAt;
}
