package com.unify.app.notifications.web;

import com.unify.app.notifications.domain.NotificationService;
import com.unify.app.notifications.domain.models.NotificationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
class NotificationController {

  private final NotificationService notificationService;
  private final SimpMessagingTemplate messagingTemplate;

  // ✅ UPDATED: REST API - Get notifications with pagination
  @GetMapping("/{userId}")
  public ResponseEntity<NotificationResponse> getUserNotifications(
      @PathVariable String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    try {
      if (userId == null || userId.trim().isEmpty()) {
        log.warn("Invalid userId: {}", userId);
        return ResponseEntity.badRequest().body(new NotificationResponse(List.of(), 0, 0, 0, 0));
      }

      Pageable pageable = PageRequest.of(page, Math.min(size, 100)); // Limit max size to 100
      Page<NotificationDto> notifications =
          notificationService.getNotificationsForUser(userId, pageable);
      long unreadCount = notificationService.getUnreadCount(userId);

      NotificationResponse response =
          new NotificationResponse(
              notifications.getContent(),
              notifications.getTotalElements(),
              notifications.getTotalPages(),
              notifications.getNumber(),
              unreadCount);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Failed to get notifications for user {}: {}", userId, e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body(new NotificationResponse(List.of(), 0, 0, 0, 0));
    }
  }

  // ✅ NEW: Get unread count only
  @GetMapping("/{userId}/unread-count")
  public ResponseEntity<UnreadCountResponse> getUnreadCount(@PathVariable String userId) {
    try {
      if (userId == null || userId.trim().isEmpty()) {
        log.warn("Invalid userId for unread count: {}", userId);
        return ResponseEntity.badRequest().body(new UnreadCountResponse(0L));
      }

      long unreadCount = notificationService.getUnreadCount(userId);

      return ResponseEntity.ok(new UnreadCountResponse(unreadCount));
    } catch (Exception e) {
      log.error("Failed to get unread count for user {}: {}", userId, e.getMessage(), e);
      return ResponseEntity.internalServerError().body(new UnreadCountResponse(0L));
    }
  }

  // ✅ UPDATED: Mark single notification as read with better error handling
  @PostMapping("/mark-as-read")
  public ResponseEntity<?> markAsRead(@RequestBody MarkAsReadRequest request) {
    try {
      if (request.notificationId() == null || request.userId() == null) {
        log.warn("Invalid markAsRead request: {}", request);
        return ResponseEntity.badRequest().body("Invalid request parameters");
      }

      notificationService.markAsRead(request.notificationId(), request.userId());

      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      log.warn("Invalid markAsRead request: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      log.error("Failed to mark notification as read: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body("Failed to mark notification as read: " + e.getMessage());
    }
  }

  // ✅ NEW: Mark notifications as read when modal closes
  @PostMapping("/mark-as-read-on-modal-close")
  public ResponseEntity<?> markAsReadOnModalClose(
      @RequestBody MarkAsReadOnModalCloseRequest request) {
    try {
      if (request.userId() == null) {
        log.warn("Invalid markAsReadOnModalClose request: {}", request);
        return ResponseEntity.badRequest().body("Invalid user ID");
      }

      // Mark all unread notifications as read when modal closes
      notificationService.markAllAsRead(request.userId());

      return ResponseEntity.ok().build();
    } catch (Exception e) {
      log.error("Failed to mark notifications as read on modal close: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body("Failed to mark notifications as read on modal close: " + e.getMessage());
    }
  }

  // ✅ UPDATED: Mark all notifications as read with better error handling
  @PatchMapping("/mark-all-as-read")
  public ResponseEntity<?> markAllAsRead(@RequestBody MarkAllAsReadRequest request) {
    try {
      if (request.userId() == null || request.userId().trim().isEmpty()) {
        log.warn("Invalid markAllAsRead request: {}", request);
        return ResponseEntity.badRequest().body("Invalid user ID");
      }

      notificationService.markAllAsRead(request.userId());

      return ResponseEntity.ok().build();
    } catch (Exception e) {
      log.error("Failed to mark all notifications as read: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body("Failed to mark all notifications as read: " + e.getMessage());
    }
  }

  // ✅ NEW: WebSocket message handlers for notification acknowledgments
  @MessageMapping("/notifications/ack")
  public void handleNotificationAck(@Payload String ackMessage) {
    try {
      // Log connection acknowledgment
      log.info("User notification acknowledgment received: {}", ackMessage);
    } catch (Exception e) {
      log.error("Failed to handle notification acknowledgment: {}", e.getMessage(), e);
    }
  }

  @MessageMapping("/notifications/received")
  public void handleNotificationReceived(@Payload String receivedMessage) {
    try {
      // Log notification received acknowledgment
      log.debug("Notification received acknowledgment: {}", receivedMessage);
    } catch (Exception e) {
      log.error("Failed to handle notification received acknowledgment: {}", e.getMessage(), e);
    }
  }

  // WebSocket - Create via message (optional)
  @MessageMapping("/send")
  @SendToUser("/queue/notifications")
  public void sendNotification(NotificationDto dto) {
    try {
      messagingTemplate.convertAndSendToUser(dto.getReceiver(), "/queue/notifications", dto);

    } catch (Exception e) {
      log.error("Failed to send WebSocket notification: {}", e.getMessage(), e);
    }
  }

  // ✅ NEW: Response DTOs for better API structure
  public record NotificationResponse(
      List<NotificationDto> notifications,
      long totalElements,
      int totalPages,
      int currentPage,
      long unreadCount) {}

  public record UnreadCountResponse(long unreadCount) {}

  // Request DTOs
  record MarkAsReadRequest(String notificationId, String userId) {}

  record MarkAllAsReadRequest(String userId) {}

  record MarkAsReadOnModalCloseRequest(String userId) {}
}
