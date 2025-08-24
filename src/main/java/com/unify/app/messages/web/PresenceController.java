package com.unify.app.messages.web;

import com.unify.app.messages.domain.PresenceService;
import com.unify.app.messages.events.TypingEvent;
import com.unify.app.messages.events.UserStatus;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/presence")
@RequiredArgsConstructor
public class PresenceController {

  private final PresenceService presenceService;
  private final SimpMessagingTemplate messagingTemplate;

  // When a user subscribes to their presence channel

  @GetMapping("/{userId}")
  public LocalDateTime getPresence(@PathVariable String userId) {
    return presenceService.getLastActive(userId);
  }

  @MessageMapping("/presence")
  public void subscribePresence(@Payload String userId) {
    presenceService.setActive(userId);
    broadcastStatus(userId, true);
    broadcastOnlineUsers(); // push updated online list
  }

  // Custom endpoint to set inactive (when user leaves or disconnects)
  @MessageMapping("/presence/inactive")
  public void setInactive(@Payload String userId) {
    presenceService.setInactive(userId);
    broadcastStatus(userId, false);
    broadcastOnlineUsers(); // push updated online list
  }

  @MessageMapping("/typing")
  public void handleTyping(@Payload TypingEvent typingEvent) {
    // Broadcast typing event to ALL users (not just the target)
    messagingTemplate.convertAndSend("/topic/typing", typingEvent);

    // Also send to specific user for direct delivery
    messagingTemplate.convertAndSend("/topic/typing." + typingEvent.toUser(), typingEvent);
  }

  // Client requests online users explicitly
  @MessageMapping("/presence/request-online-users")
  public void requestOnlineUsers(@Payload String requesterId) {
    Set<String> onlineUsers = presenceService.getOnlineUsers();
    messagingTemplate.convertAndSendToUser(requesterId, "/queue/online-users", onlineUsers);
  }

  private void broadcastStatus(String userId, boolean active) {
    UserStatus status = new UserStatus(userId, active, presenceService.getLastActive(userId));
    messagingTemplate.convertAndSend("/topic/status", status);
  }

  private void broadcastOnlineUsers() {
    Set<String> onlineUsers = presenceService.getOnlineUsers();
    for (String userId : onlineUsers) {
      messagingTemplate.convertAndSend("/queue/" + userId + "/online-users", onlineUsers);
    }
  }
}
