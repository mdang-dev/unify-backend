package com.unify.app.messages.domain;

import com.unify.app.messages.domain.models.MessageType;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collation = "en")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
class Message implements Serializable {
  @Id String id;
  String sender;
  String receiver;
  String content;
  LocalDateTime timestamp;
  List<String> fileUrls;
  MessageType type;
}
