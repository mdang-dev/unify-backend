package com.unify.app.notifications.domain;

import com.unify.app.notifications.domain.models.NotificationDto;
import com.unify.app.notifications.domain.models.NotificationType;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
      // Don't throw exception here to avoid breaking the main flow
    }
  }

  public void createAndSendNotification(
      String senderId, String receiverId, NotificationType type, String message, String link) {
    createAndSendNotification(senderId, receiverId, type, message, link, null);
  }

  // ✅ NEW: Overload with data parameter
  public void createAndSendNotification(
      String senderId,
      String receiverId,
      NotificationType type,
      String message,
      String link,
      String data) {
    try {
      // Validate inputs
      if (senderId == null || receiverId == null || type == null) {
        log.warn(
            "Invalid notification parameters: senderId={}, receiverId={}, type={}",
            senderId,
            receiverId,
            type);
        return;
      }

      // Don't send notification to self
      if (senderId.equals(receiverId)) {
        return;
      }

      // ✅ NEW: Handle duplicate notifications - replace old with new
      handleDuplicateNotification(senderId, receiverId, type);

      Notification notification =
          Notification.builder()
              .sender(senderId)
              .receiver(receiverId)
              .type(type)
              .message(message)
              .link(link)
              .data(data) // ✅ ADDED: Include data field
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

      log.info(
          "Notification created and sent: type={}, sender={}, receiver={}",
          type,
          senderId,
          receiverId);
    } catch (Exception e) {
      log.error("Failed to create and send notification: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create and send notification", e);
    }
  }

  // Overload for backward compatibility
  public void createAndSendNotification(String senderId, String receiverId, NotificationType type) {
    createAndSendNotification(senderId, receiverId, type, generateMessage(senderId, type), null);
  }

  // ✅ NEW: Paginated notifications with better performance
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

      // Get unique sender IDs for efficient user fetching
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

  // ✅ NEW: Get unread count for performance optimization
  public long getUnreadCount(String receiverId) {
    try {
      if (receiverId == null || receiverId.trim().isEmpty()) {
        return 0L;
      }
      Long count = notificationRepository.countByReceiverAndIsReadFalse(receiverId);
      return count != null ? count : 0L;
    } catch (Exception e) {
      log.error("Failed to get unread count for user {}: {}", receiverId, e.getMessage(), e);
      return 0L; // Return 0 instead of throwing to avoid breaking UI
    }
  }

  // ✅ NEW: Get unread count by type
  public long getUnreadCountByType(String receiverId, NotificationType type) {
    try {
      if (receiverId == null || receiverId.trim().isEmpty() || type == null) {
        return 0L;
      }
      Long count = notificationRepository.countByReceiverAndTypeAndIsReadFalse(receiverId, type);
      return count != null ? count : 0L;
    } catch (Exception e) {
      log.error(
          "Failed to get unread count by type for user {}: {}", receiverId, e.getMessage(), e);
      return 0L;
    }
  }

  // ✅ NEW: Backward compatibility method
  public List<NotificationDto> getNotificationsForUser(String receiverId) {
    try {
      Page<NotificationDto> page =
          getNotificationsForUser(
              receiverId, org.springframework.data.domain.PageRequest.of(0, 50)); // Default limit
      return page.getContent();
    } catch (Exception e) {
      log.error("Failed to get notifications for user {}: {}", receiverId, e.getMessage(), e);
      return List.of();
    }
  }

  private void handleFollowOrLikeNotification(
      String senderId, String receiverId, NotificationType type) {
    try {
      Notification existing =
          notificationRepository
              .findTopBySenderAndReceiverAndTypeOrderByTimestampDesc(senderId, receiverId, type)
              .orElse(null);

      if (existing != null) {
        if (type == NotificationType.FOLLOW) {
          LocalDateTime now = LocalDateTime.now();
          LocalDateTime lastSentTime = existing.getTimestamp();

          if (lastSentTime.isAfter(now.minusMinutes(1))) {
            log.debug("Skipping follow notification - too recent for user: {}", receiverId);
            return;
          }
        }

        notificationRepository.deleteBySenderAndReceiverAndType(senderId, receiverId, type);
      }

      if (type == NotificationType.FOLLOW) {
        new Thread(
                () -> {
                  try {
                    Thread.sleep(3000);
                    sendNewNotification(senderId, receiverId, type);
                  } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Follow notification thread interrupted");
                  }
                })
            .start();
      } else {
        sendNewNotification(senderId, receiverId, type);
      }
    } catch (Exception e) {
      log.error("Failed to handle follow/like notification: {}", e.getMessage(), e);
    }
  }

  private void sendNewNotification(String senderId, String receiverId, NotificationType type) {
    createAndSendNotification(senderId, receiverId, type, generateMessage(senderId, type), null);
  }

  public void markAllAsRead(String receiverId) {
    try {
      if (receiverId == null || receiverId.trim().isEmpty()) {
        log.warn("Invalid receiverId for markAllAsRead: {}", receiverId);
        return;
      }

      List<Notification> notifications =
          notificationRepository.findByReceiverOrderByTimestampDesc(receiverId);
      notifications.forEach(notification -> notification.setRead(true));
      notificationRepository.saveAll(notifications);

      log.info("Marked all notifications as read for user: {}", receiverId);
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

      // Verify the notification belongs to the user
      if (!notification.getReceiver().equals(receiverId)) {
        log.warn("Notification {} does not belong to user {}", notificationId, receiverId);
        throw new IllegalArgumentException("Notification does not belong to user");
      }

      notification.setRead(true);
      notificationRepository.save(notification);

      log.debug("Marked notification {} as read for user: {}", notificationId, receiverId);
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

  private String generateMessage(String senderId, NotificationType type) {
    return switch (type) {
      case FOLLOW -> senderId + " is following you.";
      case LIKE -> senderId + " liked your post.";
      case COMMENT -> senderId + " commented on your post.";
      case MESSAGE -> senderId + " sent you a message.";
      case TAG -> senderId + " tagged you in a post.";
      case SHARE -> senderId + " shared your post.";
      default -> "You have a new notification.";
    };
  }

  // ✅ NEW: Handle duplicate notifications - replace old with new
  private void handleDuplicateNotification(
      String senderId, String receiverId, NotificationType type) {
    try {
      // Find existing notification of the same type from the same sender
      Notification existing =
          notificationRepository
              .findTopBySenderAndReceiverAndTypeOrderByTimestampDesc(senderId, receiverId, type)
              .orElse(null);

      if (existing != null) {
        // Delete the old notification to replace with new one
        notificationRepository.deleteBySenderAndReceiverAndType(senderId, receiverId, type);
        log.debug(
            "Deleted duplicate notification: type={}, sender={}, receiver={}",
            type,
            senderId,
            receiverId);
      }
    } catch (Exception e) {
      log.error("Failed to handle duplicate notification: {}", e.getMessage(), e);
      // Don't throw exception to avoid breaking the main flow
    }
  }
}
