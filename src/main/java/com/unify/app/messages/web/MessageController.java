package com.unify.app.messages.web;

import com.unify.app.messages.domain.MessageService;
import com.unify.app.messages.domain.models.ChatDto;
import com.unify.app.messages.domain.models.MessageDto;
import com.unify.app.ws.WebSocketPerformanceMonitor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
            "timestamp", java.time.LocalDateTime.now().toString()));
  }

  @MessageMapping("/chat.sendMessage")
  public void sendMessage(@Payload MessageDto message) {
    try {
      // ✅ PERFORMANCE: Ultra-fast message processing
      MessageDto updateMessage = MessageDto.withCurrentTimestamp(message);

      // ✅ PERFORMANCE: Send message to receiver immediately (async save)
      messagingTemplate.convertAndSendToUser(
          updateMessage.receiver(), "/queue/messages", updateMessage);

      // ✅ PERFORMANCE: Async save and broadcast (non-blocking)
      CompletableFuture.runAsync(
          () -> {
            try {
              // Save message to database
              messageService.saveMessage(updateMessage);

              // Broadcast chat list updates to both users
              broadcastChatListUpdate(updateMessage.sender(), updateMessage.receiver());

            } catch (Exception e) {
              // Silent error handling
            }
          });

    } catch (Exception e) {
      // Silent error handling
      messagingTemplate.convertAndSendToUser(
          message.sender(),
          "/queue/errors",
          Map.of("error", "Failed to send message", "timestamp", System.currentTimeMillis()));
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
      // Save message to database
      MessageDto savedMessage = messageService.saveMessage(message);

      // ✅ FIX: Send message to receiver only via WebSocket
      messagingTemplate.convertAndSendToUser(
          savedMessage.receiver(), "/queue/messages", savedMessage);

      // Broadcast chat list updates to both users
      broadcastChatListUpdate(message.sender(), message.receiver());

      return ResponseEntity.ok(savedMessage);

    } catch (Exception e) {
      log.error("Error sending message via HTTP: {}", e.getMessage());
      return ResponseEntity.status(500)
          .body(Map.of("error", "Failed to send message", "message", e.getMessage()));
    }
  }

  @GetMapping("/chat-list/{userId}")
  public ResponseEntity<?> getChatList(@PathVariable String userId) {
    if (userId == null || userId.trim().isEmpty()) {
      return ResponseEntity.badRequest().body("User ID is required");
    }

    try {
      List<ChatDto> chatList = messageService.getChatList(userId);
      return ResponseEntity.ok(chatList);

    } catch (Exception e) {
      return ResponseEntity.status(500)
          .body(
              Map.of("error", "Internal server error", "message", "Failed to retrieve chat list"));
    }
  }
}
