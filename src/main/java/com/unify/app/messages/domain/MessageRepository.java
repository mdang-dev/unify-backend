package com.unify.app.messages.domain;

import com.unify.app.messages.domain.models.ChatPreviewProjection;
import java.util.List;
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

  @Aggregation(
      pipeline = {
        "{ $match: { $or: [ { sender: ?0 }, { receiver: ?0 } ] } }",
        "{ $sort: { timestamp: -1 } }",
        "{ $group: { "
            + "_id: { $cond: [ { $eq: [ '$sender', ?0 ] }, '$receiver', '$sender' ] }, "
            + "lastMessage: { $first: '$content' }, "
            + "lastMessageTime: { $first: '$timestamp' } "
            + "} }"
      })
  List<ChatPreviewProjection> findChatList(String userId);
}
