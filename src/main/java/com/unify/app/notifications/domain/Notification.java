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
class Notification implements Serializable {
  @Id String id;
  String sender;
  String receiver;

  @Enumerated(EnumType.STRING)
  NotificationType type;

  LocalDateTime timestamp;
  @Builder.Default boolean isRead = false;
  String message;
  String link;
}
