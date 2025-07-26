package com.unify.app.messages.domain;

import com.unify.app.messages.domain.models.ChatDto;
import com.unify.app.messages.domain.models.ChatPreviewProjection;
import com.unify.app.messages.domain.models.MessageDto;
import com.unify.app.users.domain.UserService;
import com.unify.app.users.domain.models.UserDto;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

  private final MongoTemplate mongoTemplate;
  private final MessageRepository messageRepository;
  private final UserService userService;
  private final MessageMapper mapper;

  @Cacheable(value = "messages", key = "#sender + '-' + #receiver")
  public List<MessageDto> getMessagesBySenderAndReceiver(String sender, String receiver) {
    Query query = new Query();
    query.addCriteria(
        new Criteria()
            .orOperator(
                Criteria.where("sender").is(sender).and("receiver").is(receiver),
                Criteria.where("sender").is(receiver).and("receiver").is(sender)));
    query.collation(Collation.of("en"));
    query.with(Sort.by(Sort.Direction.ASC, "timestamp"));
    return mongoTemplate.find(query, Message.class).stream()
        .map(mapper::toDto)
        .collect(Collectors.toList());
  }

  @Cacheable(value = "chatLists", key = "#user.id")
  public List<ChatDto> getChatList(String userId) {
    List<ChatPreviewProjection> rawList = messageRepository.findChatList(userId);

    return rawList.stream()
        .map(
            chat -> {
              UserDto user = userService.findById(chat.get_id());
              return ChatDto.builder()
                  .userId(user.id())
                  .username(user.username())
                  .fullName(user.firstName() + " " + user.lastName())
                  .avatar(user.avatar())
                  .lastMessage(chat.getLastMessage())
                  .lastMessageTime(chat.getLastMessageTime())
                  .build();
            })
        .sorted(
            Comparator.comparing(
                    ChatDto::getLastMessageTime, Comparator.nullsLast(LocalDateTime::compareTo))
                .reversed())
        .collect(Collectors.toList());
  }

  @Caching(
      evict = {
        @CacheEvict(value = "messages", key = "#message.sender + '-' + #message.receiver"),
        @CacheEvict(value = "messages", key = "#message.receiver + '-' + #message.sender"),
        @CacheEvict(value = "chatLists", key = "#message.sender"),
        @CacheEvict(value = "chatLists", key = "#message.receiver")
      })
  public MessageDto saveMessage(MessageDto message) {
    Message messageEntity = mapper.toEntity(message);
    if (message.receiver() == null) {
      throw new IllegalArgumentException("Receiver must not be null");
    }
    return mapper.toDto(messageRepository.save(messageEntity));
  }
}
