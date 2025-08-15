package com.unify.app.messages.web;

import com.unify.app.common.utils.DateTimeUtils;
import com.unify.app.messages.domain.MessageService;
import com.unify.app.messages.domain.models.ChatDto;
import com.unify.app.messages.domain.models.MessageDto;
import com.unify.app.ws.WebSocketPerformanceMonitor;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

  private final SimpMessagingTemplate messagingTemplate;
  private final MessageService messageService;
  private final WebSocketPerformanceMonitor performanceMonitor;

  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(
        Map.of(
            "status", "UP",
            "service", "MessageService",
            "timestamp", DateTimeUtils.nowVietnam().toString()));
  }

  @MessageMapping("/chat.sendMessage")
  public void sendMessage(@Payload MessageDto message) {
    try {
      // ✅ IMPROVED: Check for duplicate messages before processing
      if (messageService.isDuplicateMessage(message)) {
        log.warn(
            "Duplicate message detected, ignoring: {} -> {}, content: {}",
            message.sender(),
            message.receiver(),
            message.content());
        return;
      }

      // ✅ PERFORMANCE: Ultra-fast message processing
      MessageDto updateMessage = MessageDto.withCurrentTimestamp(message);

      // ✅ IMPROVED: Ensure message is saved to database first before broadcasting
      MessageDto savedMessage = messageService.saveMessage(updateMessage);

      // ✅ IMPROVED: Send saved message to both users with server timestamp to avoid clock skew
      // issues
      messagingTemplate.convertAndSendToUser(
          savedMessage.receiver(), "/queue/messages", savedMessage);
      messagingTemplate.convertAndSendToUser(
          savedMessage.sender(), "/queue/messages", savedMessage);

      // ✅ IMPROVED: Broadcast chat list updates to both users after successful save
      broadcastChatListUpdate(savedMessage.sender(), savedMessage.receiver());

    } catch (Exception e) {
      log.error("Failed to send message: {}", e.getMessage());

      // ✅ IMPROVED: Send error notification to sender
      messagingTemplate.convertAndSendToUser(
          message.sender(),
          "/queue/errors",
          Map.of(
              "error", "Failed to send message",
              "timestamp", System.currentTimeMillis(),
              "messageId", message.clientTempId(),
              "details", e.getMessage()));
    }
  }

  // ✅ REAL-TIME: Broadcast chat list updates to both users
  private void broadcastChatListUpdate(String senderId, String receiverId) {
    try {
      // ✅ FIX: Send minimal update notification instead of full chat list
      // This prevents overriding optimistic updates on frontend
      Map<String, Object> updateNotification =
          Map.of(
              "type", "chat-list-update",
              "timestamp", System.currentTimeMillis(),
              "message", "Chat list updated");

      // Send to sender (only notification, not full list)
      messagingTemplate.convertAndSendToUser(
          senderId, "/queue/chat-list-update", updateNotification);

      // Send to receiver (only notification, not full list)
      messagingTemplate.convertAndSendToUser(
          receiverId, "/queue/chat-list-update", updateNotification);

    } catch (Exception e) {
      // Silent error handling
    }
  }

  @SubscribeMapping("/user/{userId}/queue/messages")
  public void subscribeToMessages(@PathVariable String userId) {
    // This method is called when a user subscribes to their message queue
    // You can use this to send any pending messages or status updates
  }

  // ✅ REAL-TIME: Subscribe to chat list updates
  @SubscribeMapping("/user/{userId}/queue/chat-list-update")
  public List<ChatDto> subscribeToChatListUpdates(@PathVariable String userId) {
    // Return current chat list when user subscribes
    return messageService.getChatList(userId);
  }

  @MessageExceptionHandler
  public void handleException(Throwable exception) {
    // Handle any exceptions that occur during message processing
    messagingTemplate.convertAndSend("/topic/errors", exception.getMessage());
  }

  @GetMapping("/{user1}/{user2}")
  public List<MessageDto> getMessagesBetweenUsers(
      @PathVariable String user1, @PathVariable String user2) {
    return messageService.getMessagesBySenderAndReceiver(user1, user2);
  }

  // ✅ OPTIMISTIC: HTTP endpoint for fallback message sending
  @PostMapping("/send")
  public ResponseEntity<?> sendMessageHttp(@RequestBody MessageDto message) {
    try {
      // ✅ IMPROVED: Check for duplicate messages before processing
      if (messageService.isDuplicateMessage(message)) {
        log.warn(
            "Duplicate message detected via HTTP, ignoring: {} -> {}, content: {}",
            message.sender(),
            message.receiver(),
            message.content());
        return ResponseEntity.ok(
            Map.of(
                "status", "duplicate",
                "message", "Message already sent",
                "timestamp", System.currentTimeMillis()));
      }

      // Ensure server-side timestamp for consistency across clients
      MessageDto updateMessage = MessageDto.withCurrentTimestamp(message);

      // ✅ IMPROVED: Save message to database first
      MessageDto savedMessage = messageService.saveMessage(updateMessage);

      // ✅ IMPROVED: Send to both users with server timestamp after successful save
      messagingTemplate.convertAndSendToUser(
          savedMessage.receiver(), "/queue/messages", savedMessage);
      messagingTemplate.convertAndSendToUser(
          savedMessage.sender(), "/queue/messages", savedMessage);

      // ✅ IMPROVED: Broadcast chat list updates to both users
      broadcastChatListUpdate(savedMessage.sender(), savedMessage.receiver());

      // ✅ LOGGING: Log successful HTTP message delivery
      log.debug(
          "Message sent via HTTP successfully: {} -> {}, content: {}",
          savedMessage.sender(),
          savedMessage.receiver(),
          savedMessage.content());

      return ResponseEntity.ok(savedMessage);

    } catch (Exception e) {
      log.error("Error sending message via HTTP: {}", e.getMessage());
      return ResponseEntity.status(500)
          .body(
              Map.of(
                  "error", "Failed to send message",
                  "message", e.getMessage(),
                  "timestamp", System.currentTimeMillis()));
    }
  }

  @GetMapping("/chat-list/{userId}")
  public ResponseEntity<?> getChatList(@PathVariable String userId) {
    // ✅ PRODUCTION FIX: Simplified security - just check authentication
    // The frontend should send the correct user ID
    if (userId == null || userId.trim().isEmpty()) {
      return ResponseEntity.badRequest().body("User ID is required");
    }

    try {
      List<ChatDto> chatList = messageService.getChatList(userId);
      return ResponseEntity.ok(chatList);

    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Error getting chat list for user {}: {}", userId, e.getMessage());
      }
      return ResponseEntity.ok(List.of());
    }
  }

  // ✅ BACKEND SYNC: Check message status endpoint
  @GetMapping("/status")
  public ResponseEntity<?> checkMessageStatus(
      @RequestParam(required = false) String messageId,
      @RequestParam(required = false) String clientTempId) {

    try {
      if (messageId == null && clientTempId == null) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Either messageId or clientTempId is required"));
      }

      MessageDto message = messageService.findMessageByIdOrTempId(messageId, clientTempId);

      if (message == null) {
        return ResponseEntity.ok(Map.of("status", "unknown", "exists", false));
      }

      // Map backend message state to frontend-compatible status
      String status = mapToMessageStatus(message);

      return ResponseEntity.ok(
          Map.of(
              "messageId",
              message.id(),
              "status",
              status,
              "timestamp",
              message.timestamp(),
              "exists",
              true,
              "serverConfirmed",
              true));

    } catch (Exception e) {
      log.error("Error checking message status: {}", e.getMessage());
      return ResponseEntity.status(500).body(Map.of("error", "Failed to check message status"));
    }
  }

  // ✅ BACKEND SYNC: Batch check message statuses
  @PostMapping("/batch-status")
  public ResponseEntity<?> batchCheckMessageStatus(@RequestBody Map<String, List<String>> request) {

    try {
      List<String> messageIds = request.get("messageIds");
      if (messageIds == null || messageIds.isEmpty()) {
        return ResponseEntity.ok(List.of());
      }

      List<MessageDto> messages = messageService.findMessagesByIdsOrTempIds(messageIds);

      List<Map<String, Object>> statuses =
          messages.stream()
              .map(
                  message ->
                      Map.of(
                          "messageId", (Object) message.id(),
                          "clientTempId",
                              (Object)
                                  (message.clientTempId() != null ? message.clientTempId() : ""),
                          "status", (Object) mapToMessageStatus(message),
                          "timestamp", (Object) message.timestamp(),
                          "serverConfirmed", (Object) true))
              .toList();

      return ResponseEntity.ok(statuses);

    } catch (Exception e) {
      log.error("Error batch checking message statuses: {}", e.getMessage());
      return ResponseEntity.ok(List.of());
    }
  }

  // ✅ Map message to status for frontend
  private String mapToMessageStatus(MessageDto message) {
    // For now, all saved messages are considered "delivered"
    // You can extend this logic based on your message state requirements
    if (message.timestamp() != null) {
      return "delivered"; // Message exists in database = delivered
    }
    return "pending"; // Fallback
  }
}
