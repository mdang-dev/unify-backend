package com.unify.app.notifications.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationDto implements Serializable {
  String id;
  SenderDto sender;
  String receiver;
  NotificationType type;
  String message;
  LocalDateTime timestamp;

  @JsonProperty("isRead") // ✅ FIX: Ensure isRead field is properly serialized
  @Default
  boolean isRead = false;

  String link;
  String data; // ✅ ADDED: Store JSON data like commentId, postId

  @Getter
  @Setter
  @Builder
  public static class SenderDto {
    String id;
    String fullName;
    String avatar;
  }
}
