package com.unify.app.messages.domain.models;

import java.time.LocalDateTime;

public interface ChatPreviewProjection {
  String get_id(); // userId of the other person

  String getLastMessage();

  LocalDateTime getLastMessageTime();

  String getLastMessageSender(); // ✅ who sent the last message
}
