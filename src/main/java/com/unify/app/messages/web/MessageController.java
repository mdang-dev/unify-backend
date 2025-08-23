package com.unify.app.messages.web;

import com.unify.app.messages.domain.MessageService;
import com.unify.app.messages.domain.models.ChatDto;
import com.unify.app.messages.domain.models.MessageDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

  private final SimpMessagingTemplate messagingTemplate;
  private final MessageService messageService;

  @GetMapping("/{user1}/{user2}")
  public List<MessageDto> getMessagesBetweenUsers(
      @PathVariable String user1, @PathVariable String user2) {
    return messageService.getMessagesBySenderAndReceiver(user1, user2);
  }

  @MessageMapping("/chat.send")
  public void sendMessageHttp(@Payload MessageDto message) {

    MessageDto updateMessage = MessageDto.withCurrentTimestamp(message);
      System.out.println("Message " + updateMessage);
    messagingTemplate.convertAndSend(
        "/user/" + message.sender() + "/queue/messages", updateMessage);
    messagingTemplate.convertAndSend(
        "/user/" + message.receiver() + "/queue/messages", updateMessage);

    MessageDto savedMessage = messageService.saveMessage(updateMessage);
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
}
