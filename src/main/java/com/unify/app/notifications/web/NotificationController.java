package com.unify.app.notifications.web;

import com.unify.app.notifications.domain.NotificationService;
import com.unify.app.notifications.domain.models.NotificationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
class NotificationController {

  private final NotificationService notificationService;
  private final SimpMessagingTemplate messagingTemplate;

  // REST API - Get notifications
  @GetMapping("/{userId}")
  public List<NotificationDto> getUserNotifications(@PathVariable String userId) {
    return notificationService.getNotificationsForUser(userId);
  }

  // WebSocket - Create via message (optional)
  @MessageMapping("/send")
  @SendToUser("/queue/notifications")
  public void sendNotification(NotificationDto dto) {
    messagingTemplate.convertAndSendToUser(dto.getReceiver(), "/queue/notifications", dto);
  }
}
