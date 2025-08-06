package com.unify.app.notifications.web;

import com.unify.app.notifications.domain.NotificationService;
import com.unify.app.notifications.domain.models.NotificationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
class NotificationController {

  private final NotificationService notificationService;
  private final SimpMessagingTemplate messagingTemplate;

  // REST API - Get notifications
  @GetMapping("/{userId}")
  public List<NotificationDto> getUserNotifications(@PathVariable String userId) {
    return notificationService.getNotificationsForUser(userId);
  }

  // Mark single notification as read
  @PostMapping("/mark-as-read")
  public ResponseEntity<?> markAsRead(@RequestBody MarkAsReadRequest request) {
    try {
      notificationService.markAsRead(request.notificationId(), request.userId());
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body("Failed to mark notification as read: " + e.getMessage());
    }
  }

  // Mark all notifications as read
  @PatchMapping("/mark-all-as-read")
  public ResponseEntity<?> markAllAsRead(@RequestBody MarkAllAsReadRequest request) {
    try {
      notificationService.markAllAsRead(request.userId());
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body("Failed to mark all notifications as read: " + e.getMessage());
    }
  }

  // WebSocket - Create via message (optional)
  @MessageMapping("/send")
  @SendToUser("/queue/notifications")
  public void sendNotification(NotificationDto dto) {
    messagingTemplate.convertAndSendToUser(dto.getReceiver(), "/queue/notifications", dto);
  }

  // Request DTOs
  record MarkAsReadRequest(String notificationId, String userId) {}

  record MarkAllAsReadRequest(String userId) {}
}
