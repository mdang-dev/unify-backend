package com.unify.app.messages.domain;

import com.unify.app.messages.domain.models.ChatPreviewProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

interface MessageRepository extends MongoRepository<Message, String> {
  @Query(
      value =
          "{ $or: [ { $and: [ { sender: ?0 }, { receiver: ?1 } ] }, { $and: [ { sender: ?2 }, { receiver: ?3 } ] } ] }")
  List<Message> findMessages(
      String sender1, String receiver1, String sender2, String receiver2, Sort sort);

  // Optimized method using predefined sort for ascending timeline
  @Query(
      value =
          "{ $or: [ { $and: [ { sender: ?0 }, { receiver: ?1 } ] }, { $and: [ { sender: ?1 }, { receiver: ?0 } ] } ] }",
      fields = "{ id: 1, sender: 1, receiver: 1, content: 1, timestamp: 1, fileUrls: 1 }")
  List<Message> findConversationAsc(String userId, String partnerId, Sort sort);

  @Aggregation(
      pipeline = {
        "{ $match: { $or: [ { sender: ?0 }, { receiver: ?0 } ] } }",
        "{ $sort: { timestamp: -1 } }",
        "{ $group: { "
            + "_id: { $cond: [ { $eq: [ '$sender', ?0 ] }, '$receiver', '$sender' ] }, "
            + "lastMessage: { $first: '$content' }, "
            + "lastMessageTime: { $first: '$timestamp' } "
            + "} }",
        "{ $match: { _id: { $ne: null } } }", // Filter out null _id values
        "{ $match: { _id: { $ne: '' } } }", // Filter out empty _id values
        "{ $match: { _id: { $exists: true } } }" // Ensure _id exists
      })
  List<ChatPreviewProjection> findChatList(String userId);

  // ✅ BACKEND SYNC: Find message by clientTempId
  Optional<Message> findByClientTempId(String clientTempId);

  // ✅ BACKEND SYNC: Find messages by multiple clientTempIds
  List<Message> findByClientTempIdIn(List<String> clientTempIds);

  // ✅ IMPROVED: Find messages by clientTempId and timestamp after
  @Query(value = "{ 'clientTempId': ?0, 'timestamp': { $gte: ?1 } }")
  List<Message> findByClientTempIdAndTimestampAfter(String clientTempId, LocalDateTime timestamp);

  // ✅ IMPROVED: Find recent messages by content and users to detect duplicates
  @Query(value = "{ 'content': ?0, 'sender': ?1, 'receiver': ?2, 'timestamp': { $gte: ?3 } }")
  List<Message> findRecentMessagesByContentAndUsers(
      String content, String sender, String receiver, LocalDateTime since);
}
