package com.unify.app.notifications.domain;

import com.unify.app.notifications.domain.models.NotificationDto;
import com.unify.app.notifications.domain.models.NotificationType;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final UserService userService;

  public Notification saveNotification(Notification notification) {
    try {
      return notificationRepository.save(notification);
    } catch (Exception e) {
      log.error("Failed to save notification: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to save notification", e);
    }
  }

  public void sendNotification(String receiverId, NotificationDto notificationDTO) {
    try {
      simpMessagingTemplate.convertAndSend(
          "/user/" + receiverId + "/queue/notifications", notificationDTO);

    } catch (Exception e) {
      log.error("Failed to send notification to user {}: {}", receiverId, e.getMessage(), e);
    }
  }

  // Overload with 3 parameters for backward compatibility
  public void createAndSendNotification(String senderId, String receiverId, NotificationType type) {
    createAndSendNotification(senderId, receiverId, type, generateMessage(senderId, type), null);
  }

  public void createAndSendNotification(
      String senderId, String receiverId, NotificationType type, String message, String link) {
    createAndSendNotification(senderId, receiverId, type, message, link, null);
  }

  // ✅ NEW: Enhanced method for creating and sending notifications with better WebSocket handling
  public void createAndSendNotification(
      String senderId,
      String receiverId,
      NotificationType type,
      String message,
      String link,
      String data) {
    try {
      if (senderId == null || receiverId == null || type == null) {
        log.warn(
            "Invalid notification parameters: senderId={}, receiverId={}, type={}",
            senderId,
            receiverId,
            type);
        return;
      }

      if (senderId.equals(receiverId)) {
        return;
      }

      // ✅ ENHANCED: Handle different notification types with optimized logic
      switch (type) {
        case FOLLOW -> createFollowNotification(senderId, receiverId, message, link, data);
        case TAG -> createTagNotification(senderId, receiverId, message, link, data);
        case SHARE -> createShareNotification(senderId, receiverId, message, link, data);
        case REPORT, POST_REPORT, COMMENT_REPORT, USER_REPORT -> createReportNotification(
            senderId, receiverId, type, message, link, data);
        default -> createStandardNotification(senderId, receiverId, type, message, link, data);
      }

    } catch (Exception e) {
      log.error("Failed to create and send notification: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create and send notification", e);
    }
  }

  // ✅ NEW: Special method for follow notifications to prevent duplicates
  private void createFollowNotification(
      String senderId, String receiverId, String message, String link, String data) {
    try {
      // Check if a follow notification already exists
      Optional<Notification> existingNotification =
          notificationRepository.findTopBySenderAndReceiverAndTypeOrderByTimestampDesc(
              senderId, receiverId, NotificationType.FOLLOW);

      if (existingNotification.isPresent()) {
        // Delete existing follow notification
        log.info("Deleting existing follow notification from {} to {}", senderId, receiverId);
        notificationRepository.deleteBySenderAndReceiverAndType(
            senderId, receiverId, NotificationType.FOLLOW);
      }

      // Create new follow notification
      Notification notification =
          Notification.builder()
              .sender(senderId)
              .receiver(receiverId)
              .type(NotificationType.FOLLOW)
              .message(
                  message != null ? message : generateMessage(senderId, NotificationType.FOLLOW))
              .link(link)
              .data(data)
              .timestamp(LocalDateTime.now())
              .isRead(false)
              .build();

      Notification savedNotification = saveNotification(notification);

      // Send via WebSocket
      List<User> users = userService.findAllById(List.of(senderId, receiverId));
      Map<String, User> userMap =
          users.stream().collect(Collectors.toMap(User::getId, user -> user));
      NotificationDto notificationDTO =
          notificationMapper.toNotificationDTO(savedNotification, userMap);
      sendNotification(receiverId, notificationDTO);

      log.info("Created new follow notification from {} to {}", senderId, receiverId);

    } catch (Exception e) {
      log.error("Failed to create follow notification: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create follow notification", e);
    }
  }

  // ✅ NEW: Method for tag notifications
  private void createTagNotification(
      String senderId, String receiverId, String message, String link, String data) {
    try {
      Notification notification =
          Notification.builder()
              .sender(senderId)
              .receiver(receiverId)
              .type(NotificationType.TAG)
              .message(message != null ? message : generateMessage(senderId, NotificationType.TAG))
              .link(link)
              .data(data)
              .timestamp(LocalDateTime.now())
              .isRead(false)
              .build();

      Notification savedNotification = saveNotification(notification);
      sendNotificationViaWebSocket(receiverId, savedNotification);

      log.info("Created tag notification from {} to {}", senderId, receiverId);
    } catch (Exception e) {
      log.error("Failed to create tag notification: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create tag notification", e);
    }
  }

  // ✅ NEW: Method for share notifications
  private void createShareNotification(
      String senderId, String receiverId, String message, String link, String data) {
    try {
      Notification notification =
          Notification.builder()
              .sender(senderId)
              .receiver(receiverId)
              .type(NotificationType.SHARE)
              .message(
                  message != null ? message : generateMessage(senderId, NotificationType.SHARE))
              .link(link)
              .data(data)
              .timestamp(LocalDateTime.now())
              .isRead(false)
              .build();

      Notification savedNotification = saveNotification(notification);
      sendNotificationViaWebSocket(receiverId, savedNotification);

      log.info("Created share notification from {} to {}", senderId, receiverId);
    } catch (Exception e) {
      log.error("Failed to create share notification: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create share notification", e);
    }
  }

  // ✅ NEW: Method for report notifications
  private void createReportNotification(
      String senderId,
      String receiverId,
      NotificationType type,
      String message,
      String link,
      String data) {
    try {
      Notification notification =
          Notification.builder()
              .sender(senderId)
              .receiver(receiverId)
              .type(type)
              .message(message != null ? message : generateMessage(senderId, type))
              .link(link)
              .data(data)
              .timestamp(LocalDateTime.now())
              .isRead(false)
              .build();

      Notification savedNotification = saveNotification(notification);
      sendNotificationViaWebSocket(receiverId, savedNotification);

      log.info("Created {} notification from {} to {}", type, senderId, receiverId);
    } catch (Exception e) {
      log.error("Failed to create {} notification: {}", type, e.getMessage(), e);
      throw new RuntimeException("Failed to create " + type + " notification", e);
    }
  }

  // ✅ NEW: Method for creating standard (non-follow) notifications
  private void createStandardNotification(
      String senderId,
      String receiverId,
      NotificationType type,
      String message,
      String link,
      String data) {
    try {
      Notification notification =
          Notification.builder()
              .sender(senderId)
              .receiver(receiverId)
              .type(type)
              .message(message != null ? message : generateMessage(senderId, type))
              .link(link)
              .data(data)
              .timestamp(LocalDateTime.now())
              .isRead(false)
              .build();

      Notification savedNotification = saveNotification(notification);

      List<User> users = userService.findAllById(List.of(senderId, receiverId));
      Map<String, User> userMap =
          users.stream().collect(Collectors.toMap(User::getId, user -> user));
      NotificationDto notificationDTO =
          notificationMapper.toNotificationDTO(savedNotification, userMap);
      sendNotification(receiverId, notificationDTO);

    } catch (Exception e) {
      log.error("Failed to create standard notification: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create standard notification", e);
    }
  }

  public Page<NotificationDto> getNotificationsForUser(String receiverId, Pageable pageable) {
    try {
      if (receiverId == null || receiverId.trim().isEmpty()) {
        log.warn("Invalid receiverId: {}", receiverId);
        return Page.empty(pageable);
      }

      Page<Notification> notifications =
          notificationRepository.findByReceiverOrderByTimestampDesc(receiverId, pageable);

      if (notifications.isEmpty()) {
        return Page.empty(pageable);
      }

      List<String> senderIds =
          notifications.getContent().stream()
              .map(Notification::getSender)
              .distinct()
              .collect(Collectors.toList());

      List<User> users = userService.findAllById(senderIds);
      Map<String, User> userMap =
          users.stream().collect(Collectors.toMap(User::getId, user -> user));

      List<NotificationDto> notificationDtos =
          notifications.getContent().stream()
              .map(notification -> notificationMapper.toNotificationDTO(notification, userMap))
              .collect(Collectors.toList());

      return new PageImpl<>(notificationDtos, pageable, notifications.getTotalElements());
    } catch (Exception e) {
      log.error("Failed to get notifications for user {}: {}", receiverId, e.getMessage(), e);
      throw new RuntimeException("Failed to get notifications", e);
    }
  }

  public long getUnreadCount(String receiverId) {
    try {
      if (receiverId == null || receiverId.trim().isEmpty()) {
        return 0L;
      }
      Long count = notificationRepository.countByReceiverAndIsReadFalse(receiverId);
      return count != null ? count : 0L;
    } catch (Exception e) {
      log.error("Failed to get unread count for user {}: {}", receiverId, e.getMessage(), e);
      return 0L;
    }
  }

  public List<NotificationDto> getNotificationsForUser(String receiverId) {
    try {
      Page<NotificationDto> page =
          getNotificationsForUser(
              receiverId, org.springframework.data.domain.PageRequest.of(0, 50));
      return page.getContent();
    } catch (Exception e) {
      log.error("Failed to get notifications for user {}: {}", receiverId, e.getMessage(), e);
      return List.of();
    }
  }

  public void markAllAsRead(String receiverId) {
    try {
      if (receiverId == null || receiverId.trim().isEmpty()) {
        log.warn("Invalid receiverId for markAllAsRead: {}", receiverId);
        return;
      }

      List<Notification> notifications =
          notificationRepository.findByReceiverOrderByTimestampDesc(receiverId);
      notifications.forEach(notification -> notification.setIsRead(true));
      notificationRepository.saveAll(notifications);

    } catch (Exception e) {
      log.error(
          "Failed to mark all notifications as read for user {}: {}",
          receiverId,
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to mark all notifications as read", e);
    }
  }

  public void markAsRead(String notificationId, String receiverId) {
    try {
      if (notificationId == null || receiverId == null) {
        log.warn(
            "Invalid parameters for markAsRead: notificationId={}, receiverId={}",
            notificationId,
            receiverId);
        throw new IllegalArgumentException("Invalid parameters");
      }

      Notification notification =
          notificationRepository
              .findById(notificationId)
              .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

      if (!notification.getReceiver().equals(receiverId)) {
        log.warn("Notification {} does not belong to user {}", notificationId, receiverId);
        throw new IllegalArgumentException("Notification does not belong to user");
      }

      notification.setIsRead(true);
      notificationRepository.save(notification);

    } catch (Exception e) {
      log.error(
          "Failed to mark notification {} as read for user {}: {}",
          notificationId,
          receiverId,
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to mark notification as read", e);
    }
  }

  // ✅ ENHANCED: Optimized WebSocket notification sending with retry logic
  private void sendNotificationViaWebSocket(String receiverId, Notification notification) {
    try {
      // Get user data for the notification DTO
      List<User> users = userService.findAllById(List.of(notification.getSender(), receiverId));
      Map<String, User> userMap =
          users.stream().collect(Collectors.toMap(User::getId, user -> user));

      NotificationDto notificationDTO = notificationMapper.toNotificationDTO(notification, userMap);

      // Send via WebSocket with retry logic
      sendNotificationWithRetry(receiverId, notificationDTO);

    } catch (Exception e) {
      log.error(
          "Failed to prepare WebSocket notification for user {}: {}",
          receiverId,
          e.getMessage(),
          e);
      // Fallback to database-only notification (no real-time delivery)
    }
  }

  // ✅ NEW: Retry logic for WebSocket delivery
  private void sendNotificationWithRetry(String receiverId, NotificationDto notificationDTO) {
    int maxRetries = 3;
    int retryCount = 0;

    while (retryCount < maxRetries) {
      try {
        simpMessagingTemplate.convertAndSend(
            "/user/" + receiverId + "/queue/notifications", notificationDTO);

        log.debug("Successfully sent notification to user {} via WebSocket", receiverId);
        return; // Success, exit retry loop

      } catch (Exception e) {
        retryCount++;
        log.warn(
            "WebSocket notification attempt {} failed for user {}: {}",
            retryCount,
            receiverId,
            e.getMessage());

        if (retryCount >= maxRetries) {
          log.error(
              "Failed to send notification to user {} after {} attempts: {}",
              receiverId,
              maxRetries,
              e.getMessage());
        } else {
          // Wait before retry (exponential backoff)
          try {
            Thread.sleep(100 * retryCount);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      }
    }
  }

  // ✅ ENHANCED: Improved message generation with better user names
  private String generateMessage(String senderId, NotificationType type) {
    try {
      User sender = userService.findUserById(senderId);
      String senderName =
          sender != null ? (sender.getFirstName() + " " + sender.getLastName()).trim() : "Someone";

      return switch (type) {
        case FOLLOW -> senderName + " started following you.";
        case LIKE -> senderName + " liked your post.";
        case COMMENT -> senderName + " commented on your post.";
        case MESSAGE -> senderName + " sent you a message.";
        case TAG -> senderName + " tagged you in a post.";
        case SHARE -> senderName + " shared your post.";
        case REPORT, POST_REPORT, COMMENT_REPORT, USER_REPORT -> "Your content has been reported.";
        case REPORT_APPROVED -> "Your report has been approved.";
        case ACCOUNT_SUSPENDED -> "Your account has been suspended.";
        case ACCOUNT_BANNED -> "Your account has been banned.";
        default -> "You have a new notification.";
      };
    } catch (Exception e) {
      log.warn("Failed to generate personalized message for user {}: {}", senderId, e.getMessage());
      // Fallback to generic message
      return switch (type) {
        case FOLLOW -> "Someone started following you.";
        case LIKE -> "Someone liked your post.";
        case COMMENT -> "Someone commented on your post.";
        case MESSAGE -> "Someone sent you a message.";
        case TAG -> "Someone tagged you in a post.";
        case SHARE -> "Someone shared your post.";
        case REPORT, POST_REPORT, COMMENT_REPORT, USER_REPORT -> "Your content has been reported.";
        case REPORT_APPROVED -> "Your report has been approved.";
        case ACCOUNT_SUSPENDED -> "Your account has been suspended.";
        case ACCOUNT_BANNED -> "Your account has been banned.";
        default -> "You have a new notification.";
      };
    }
  }

  // Send real-time report count update
  public void sendReportCountUpdate(String userId, int reportCount) {
    try {
      NotificationDto reportCountNotification =
          NotificationDto.builder()
              .id("REPORT_COUNT_" + System.currentTimeMillis())
              .sender(null) // System notification has no sender
              .receiver(userId)
              .type(NotificationType.SYSTEM)
              .message("Your report count has been updated to: " + reportCount)
              .timestamp(LocalDateTime.now())
              .isRead(false)
              .data("{\"type\":\"reportCountUpdate\",\"reportCount\":" + reportCount + "}")
              .build();

      simpMessagingTemplate.convertAndSend(
          "/user/" + userId + "/queue/reportCount", reportCountNotification);

    } catch (Exception e) {
      log.error("Failed to send report count update to user {}: {}", userId, e.getMessage(), e);
    }
  }
}
