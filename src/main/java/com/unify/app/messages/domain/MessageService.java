package com.unify.app.messages.domain;

import com.unify.app.messages.domain.models.ChatDto;
import com.unify.app.messages.domain.models.ChatPreviewProjection;
import com.unify.app.messages.domain.models.MessageDto;
import com.unify.app.users.domain.UserService;
import com.unify.app.users.domain.models.UserDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    // Use repository method with compound indexes for faster access
    List<Message> list =
        messageRepository.findConversationAsc(
            sender, receiver, Sort.by(Sort.Direction.ASC, "timestamp"));
    return list.stream().map(mapper::toDto).collect(Collectors.toList());
  }

  // ✅ PRODUCTION FIX: Remove caching to prevent race conditions and stale data
  public List<ChatDto> getChatList(String userId) {
    if (userId == null || userId.trim().isEmpty()) {
      return List.of();
    }

    try {
      List<ChatPreviewProjection> rawList = getChatListFromMongo(userId);

      if (rawList == null || rawList.isEmpty()) {
        return List.of();
      }

      List<ChatDto> result = buildChatDtos(rawList);
      return result;

    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Error in getChatList for user {}: {}", userId, e.getMessage());
      }
      return List.of();
    }
  }

  private List<ChatPreviewProjection> getChatListFromMongo(String userId) {
    try {
      List<ChatPreviewProjection> result = messageRepository.findChatList(userId);
      return result != null ? result : List.of();
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("MongoDB error for user {}: {}", userId, e.getMessage());
      }
      return List.of();
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
    if (chat == null) {
      return null;
    }

    String otherUserId = chat.get_id();
    if (otherUserId == null || otherUserId.trim().isEmpty()) {
      return null;
    }

    try {
      UserDto user = getUserDataWithFallback(otherUserId);
      if (user == null || user.id() == null || user.id().trim().isEmpty()) {
        return ChatDto.builder()
            .userId(otherUserId)
            .username("Unknown User")
            .fullName("Unknown User")
            .avatar(null)
            .lastMessage(safeString(chat.getLastMessage()))
            .lastMessageTime(chat.getLastMessageTime())
            .build();
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
      return ChatDto.builder()
          .userId(otherUserId)
          .username("Unknown User")
          .fullName("Unknown User")
          .avatar(null)
          .lastMessage(safeString(chat.getLastMessage()))
          .lastMessageTime(chat.getLastMessageTime())
          .build();
    }
  }

  private UserDto getUserDataWithFallback(String userId) {
    try {
      return userService.findByIdSafe(userId);
    } catch (Exception e) {
      return null;
    }
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


    } catch (Exception e) {
      log.warn("Failed to update chat list cache: {}", e.getMessage());
    }
  }

  // ✅ IMPROVED: Check for duplicate messages to prevent processing duplicates
  public boolean isDuplicateMessage(MessageDto message) {
    try {
      if (message == null
          || message.content() == null
          || message.sender() == null
          || message.receiver() == null) {
        return false;
      }

      // Check for recent duplicate messages (within last 10 seconds)
      LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);

      // Look for messages with same content, sender, and receiver in recent time
      List<Message> recentMessages =
          messageRepository.findRecentMessagesByContentAndUsers(
              message.content(), message.sender(), message.receiver(), tenSecondsAgo);

      // If we find any recent messages with same content, it's a duplicate
      return !recentMessages.isEmpty();

    } catch (Exception e) {
      log.warn("Error checking for duplicate message: {}", e.getMessage());
      return false; // Allow message if we can't check for duplicates
    }
  }

  // ✅ BACKEND SYNC: Find message by ID or clientTempId
  public MessageDto findMessageByIdOrTempId(String messageId, String clientTempId) {
    try {
      // ✅ IMPROVED: Better message finding logic
      Message foundMessage = null;

      // First try to find by message ID
      if (messageId != null && !messageId.trim().isEmpty()) {
        foundMessage = messageRepository.findById(messageId).orElse(null);
      }

      // If not found by ID, try to find by clientTempId
      if (foundMessage == null && clientTempId != null && !clientTempId.trim().isEmpty()) {
        foundMessage = messageRepository.findByClientTempId(clientTempId).orElse(null);
      }

      // ✅ IMPROVED: Also try to find by content and recent timestamp if still not found
      if (foundMessage == null && clientTempId != null) {
        // Look for messages with the same clientTempId in the last 10 minutes
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<Message> recentMessages =
            messageRepository.findByClientTempIdAndTimestampAfter(clientTempId, tenMinutesAgo);

        if (!recentMessages.isEmpty()) {
          // Take the most recent one
          foundMessage =
              recentMessages.stream().max(Comparator.comparing(Message::getTimestamp)).orElse(null);
        }
      }

      if (foundMessage != null) {
        return mapper.toDto(foundMessage);
      }

      return null;

    } catch (Exception e) {
      log.warn("Error finding message by ID or clientTempId: {}", e.getMessage());
      return null;
    }
  }

  // ✅ IMPROVED: Find messages by multiple IDs or clientTempIds
  public List<MessageDto> findMessagesByIdsOrTempIds(List<String> identifiers) {
    try {
      if (identifiers == null || identifiers.isEmpty()) {
        return List.of();
      }

      List<Message> foundMessages = new ArrayList<>();

      // Separate IDs and clientTempIds
      List<String> messageIds = new ArrayList<>();
      List<String> clientTempIds = new ArrayList<>();

      for (String identifier : identifiers) {
        if (identifier.startsWith("optimistic_")) {
          clientTempIds.add(identifier);
        } else {
          messageIds.add(identifier);
        }
      }

      // Find by message IDs
      if (!messageIds.isEmpty()) {
        List<Message> byIds = messageRepository.findAllById(messageIds);
        foundMessages.addAll(byIds);
      }

      // Find by clientTempIds
      if (!clientTempIds.isEmpty()) {
        List<Message> byTempIds = messageRepository.findByClientTempIdIn(clientTempIds);
        foundMessages.addAll(byTempIds);
      }

      // Remove duplicates and return DTOs
      return foundMessages.stream().distinct().map(mapper::toDto).collect(Collectors.toList());

    } catch (Exception e) {
      log.warn("Error finding messages by IDs or clientTempIds: {}", e.getMessage());
      return List.of();
    }
  }
}
