package com.unify.app.messages.web;

import com.unify.app.messages.domain.MessageService;
import com.unify.app.messages.domain.models.MessageDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class MessageController {

  private final SimpMessagingTemplate messagingTemplate;
  private final MessageService messageService;

  @MessageMapping("/chat.sendMessage")
  public void sendMessage(@Payload MessageDto message) {

    MessageDto updateMessage = MessageDto.withCurrentTimestamp(message);
    MessageDto messageSaved = messageService.saveMessage(message);

    // Send to receiver
    messagingTemplate.convertAndSendToUser(
        updateMessage.receiver(), "/queue/messages", messageSaved);

    // Send back to sender for confirmation
    messagingTemplate.convertAndSendToUser(updateMessage.sender(), "/queue/messages", messageSaved);
  }

  @SubscribeMapping("/user/{userId}/queue/messages")
  public void subscribeToMessages(@PathVariable String userId) {
    // This method is called when a user subscribes to their message queue
    // You can use this to send any pending messages or status updates
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

  @GetMapping("/chat-list/{userId}")
  public ResponseEntity<?> getChatList(@PathVariable String userId) {
    return ResponseEntity.ok(messageService.getChatList(userId));
  }
}
