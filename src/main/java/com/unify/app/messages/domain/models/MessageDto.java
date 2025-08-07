package com.unify.app.messages.domain.models;

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
    return new MessageDto(
        message.id(),
        message.sender(),
        message.receiver(),
        message.content(),
        LocalDateTime.now(),
        message.fileUrls(),
        message.type());
  }
}
