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
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collation = "en")
@CompoundIndexes({
  // For direct conversation fetch: { sender, receiver } both orders with timestamp sort
  @CompoundIndex(
      name = "sender_receiver_ts",
      def = "{ 'sender': 1, 'receiver': 1, 'timestamp': -1 }"),
  @CompoundIndex(
      name = "receiver_sender_ts",
      def = "{ 'receiver': 1, 'sender': 1, 'timestamp': -1 }"),
  // For chat list aggregation branches
  @CompoundIndex(name = "sender_ts", def = "{ 'sender': 1, 'timestamp': -1 }"),
  @CompoundIndex(name = "receiver_ts", def = "{ 'receiver': 1, 'timestamp': -1 }")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
class Message implements Serializable {
  @Id String id;
  @Indexed String sender;
  @Indexed String receiver;
  String content;
  LocalDateTime timestamp;
  List<String> fileUrls;
  MessageType type;
}
