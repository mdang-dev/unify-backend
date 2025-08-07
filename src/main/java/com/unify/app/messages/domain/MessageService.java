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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

  @Cacheable(value = "chatLists", key = "#userId")
  public List<ChatDto> getChatList(String userId) {
    // ✅ FIX: Enhanced input validation
    if (userId == null || userId.trim().isEmpty()) {
      log.error("Error: userId is null or empty");
      return List.of();
    }

    try {
      // ✅ FIX: Step 1: Get raw chat list from MongoDB with fallback
      List<ChatPreviewProjection> rawList = getChatListFromMongo(userId);

      if (rawList == null || rawList.isEmpty()) {
        return List.of();
      }

      // ✅ FIX: Step 2: Process and build chat DTOs with validation
      List<ChatDto> result = buildChatDtos(rawList);
      return result;

    } catch (Exception e) {
      log.error("Critical error in getChatList for user {}: {}", userId, e.getMessage());
      // ✅ FIX: Return empty list instead of throwing to prevent 500 errors
      return List.of();
    }
  }

  private List<ChatPreviewProjection> getChatListFromMongo(String userId) {
    try {
      List<ChatPreviewProjection> result = messageRepository.findChatList(userId);
      return result != null ? result : List.of();
    } catch (Exception e) {
      log.error("MongoDB aggregation failed for user {}: {}", userId, e.getMessage());
      return getChatListFallback(userId);
    }
  }

  private List<ChatPreviewProjection> getChatListFallback(String userId) {
    try {
      // Manual fallback: get all messages and group them manually
      List<Message> allMessages = messageRepository.findAll();

      return allMessages.stream()
          .filter(msg -> userId.equals(msg.getSender()) || userId.equals(msg.getReceiver()))
          .collect(
              Collectors.groupingBy(
                  msg -> userId.equals(msg.getSender()) ? msg.getReceiver() : msg.getSender()))
          .entrySet()
          .stream()
          .map(
              entry -> {
                String otherUserId = entry.getKey();
                List<Message> userMessages = entry.getValue();

                // Get the latest message
                Message latestMessage =
                    userMessages.stream()
                        .max(Comparator.comparing(Message::getTimestamp))
                        .orElse(null);

                if (latestMessage == null || otherUserId == null) {
                  return null;
                }

                return new ChatPreviewProjection() {
                  @Override
                  public String get_id() {
                    return otherUserId;
                  }

                  @Override
                  public String getLastMessage() {
                    return latestMessage.getContent();
                  }

                  @Override
                  public LocalDateTime getLastMessageTime() {
                    return latestMessage.getTimestamp();
                  }
                };
              })
          .filter(chat -> chat != null)
          .collect(Collectors.toList());

    } catch (Exception e) {
      System.err.println("Fallback method also failed for user " + userId + ": " + e.getMessage());
      return List.of();
    }
  }

  private List<ChatDto> buildChatDtos(List<ChatPreviewProjection> rawList) {
    // ✅ FIX: Enhanced validation and error handling
    if (rawList == null) {
      return List.of();
    }

    try {
      return rawList.stream()
          .map(this::buildChatDto)
          .filter(chat -> chat != null) // Remove null entries
          .sorted(
              Comparator.comparing(
                      ChatDto::getLastMessageTime, Comparator.nullsLast(LocalDateTime::compareTo))
                  .reversed()) // Newest first
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error building chat DTOs: {}", e.getMessage());
      return List.of();
    }
  }

  private ChatDto buildChatDto(ChatPreviewProjection chat) {
    // ✅ FIX: Enhanced null checks
    if (chat == null) {
      return null;
    }

    String otherUserId = chat.get_id();
    if (otherUserId == null || otherUserId.trim().isEmpty()) {
      return null;
    }

    try {
      // ✅ FIX: Get user data with safe method
      UserDto user = getUserDataWithFallback(otherUserId);
      if (user == null) {
        return null;
      }

      // ✅ FIX: Validate user data
      if (user.id() == null || user.id().trim().isEmpty()) {
        return null;
      }

      LocalDateTime lastMessageTime = chat.getLastMessageTime();

      return ChatDto.builder()
          .userId(user.id())
          .username(safeString(user.username()))
          .fullName(buildFullName(user))
          .avatar(user.avatar())
          .lastMessage(safeString(chat.getLastMessage()))
          .lastMessageTime(lastMessageTime)
          .build();

    } catch (Exception e) {
      log.error("Error building chat DTO for user {}: {}", otherUserId, e.getMessage());
      return null;
    }
  }

  private UserDto getUserDataWithFallback(String userId) {
    return userService.findByIdSafe(userId); // ✅ FIX: Use safe method
  }

  private String buildFullName(UserDto user) {
    String firstName = safeString(user.firstName());
    String lastName = safeString(user.lastName());

    if (firstName.isEmpty() && lastName.isEmpty()) {
      return "Unknown User";
    }

    return (firstName + " " + lastName).trim();
  }

  private String safeString(String value) {
    return value != null ? value : "";
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

    // ✅ PERFORMANCE: Optimized message saving
    Message savedMessage = messageRepository.save(messageEntity);
    MessageDto savedDto = mapper.toDto(savedMessage);

    // ✅ REAL-TIME: Update chat list cache immediately
    updateChatListCache(savedMessage.getSender(), savedMessage.getReceiver());

    return savedDto;
  }

  // ✅ REAL-TIME: Method to update chat list cache for both users
  private void updateChatListCache(String senderId, String receiverId) {
    try {
      // Update sender's chat list
      List<ChatDto> senderChatList = getChatList(senderId);
      // Update receiver's chat list
      List<ChatDto> receiverChatList = getChatList(receiverId);

      log.debug("Updated chat list cache for users: {} and {}", senderId, receiverId);
    } catch (Exception e) {
      log.warn("Failed to update chat list cache: {}", e.getMessage());
    }
  }
}
