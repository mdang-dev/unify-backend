package com.unify.app.messages.domain.models;

import com.unify.app.common.utils.DateTimeUtils;
import java.time.LocalDateTime;
import java.util.List;
import org.bson.types.ObjectId;

public record MessageDto(
    String id,
    String sender,
    String receiver,
    String content,
    LocalDateTime timestamp,
    List<String> fileUrls,
    MessageType type,
    String clientTempId) {

  public static MessageDto withCurrentTimestamp(MessageDto message) {
    // Ensure a server-generated id exists to keep ordering stable across clients before persistence
    String ensuredId =
        (message.id() == null || message.id().isBlank())
            ? new ObjectId().toHexString()
            : message.id();

    // ✅ VIETNAM TIMEZONE: Use frontend timestamp if provided, otherwise server time
    LocalDateTime finalTimestamp =
        message.timestamp() != null ? message.timestamp() : DateTimeUtils.nowVietnam();

    return new MessageDto(
        ensuredId,
        message.sender(),
        message.receiver(),
        message.content(),
        finalTimestamp, // ✅ Use frontend or server Vietnam time
        message.fileUrls(),
        message.type(),
        message.clientTempId());
  }
}
