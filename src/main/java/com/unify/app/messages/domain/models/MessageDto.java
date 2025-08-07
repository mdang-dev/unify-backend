package com.unify.app.messages.domain.models;

import com.unify.app.common.utils.DateTimeUtils;
import java.time.LocalDateTime;
import java.util.List;

public record MessageDto(
    String id,
    String sender,
    String receiver,
    String content,
    LocalDateTime timestamp,
    List<String> fileUrls,
    MessageType type) {

  public static MessageDto withCurrentTimestamp(MessageDto message) {
    // Sử dụng timezone Việt Nam thông qua utility class
    LocalDateTime vietnamTime = DateTimeUtils.nowVietnam();

    return new MessageDto(
        message.id(),
        message.sender(),
        message.receiver(),
        message.content(),
        vietnamTime,
        message.fileUrls(),
        message.type());
  }
}
