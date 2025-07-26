package com.unify.app.media.domain.models;

import com.unify.app.users.domain.models.UserDto;
import java.time.LocalDateTime;

public record StreamDto(
    String id,
    String roomId,
    String name,
    String title,
    String description,
    String thumbnailUrl,
    UserDto user,
    Boolean isLive,
    Boolean isChatEnabled,
    Boolean isChatDelayed,
    Boolean isChatFollowersOnly,
    LocalDateTime createAt,
    LocalDateTime updateAt,
    LocalDateTime startTime,
    LocalDateTime endTime) {
  // Factory method with default values
  public static StreamDto withDefaults(
      String id,
      String roomId,
      String name,
      String title,
      String description,
      String thumbnailUrl,
      UserDto user) {
    LocalDateTime now = LocalDateTime.now();
    return new StreamDto(
        id,
        roomId,
        name,
        title,
        description,
        thumbnailUrl,
        user,
        false, // isLive
        false, // isChatEnabled
        false, // isChatDelayed
        false, // isChatFollowersOnly
        now, // createAt
        null, // updateAt
        null, // startTime
        null // endTime
        );
  }
}
