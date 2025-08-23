package com.unify.app.notifications.domain;

import com.unify.app.notifications.domain.models.NotificationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notification")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification implements Serializable {
  @Id String id;
  String sender;
  String receiver;

  @Enumerated(EnumType.STRING)
  NotificationType type;

  LocalDateTime timestamp;
  @Builder.Default boolean isRead = false;
  String message;
  String link;
  String data; // ✅ ADDED: Store JSON data like commentId, postId

  // Custom setter for isRead to ensure proper handling
  public void setIsRead(boolean isRead) {
    this.isRead = isRead;
  }

  // Manual getters and setters to ensure they exist
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getReceiver() {
    return receiver;
  }

  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }

  public NotificationType getType() {
    return type;
  }

  public void setType(NotificationType type) {
    this.type = type;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public boolean isRead() {
    return isRead;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
