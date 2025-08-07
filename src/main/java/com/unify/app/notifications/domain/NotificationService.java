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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final UserService userService;

  public Notification saveNotification(Notification notification) {
    return notificationRepository.save(notification);
  }

  public void sendNotification(String receiverId, NotificationDto notificationDTO) {
    simpMessagingTemplate.convertAndSend(
        "/user/" + receiverId + "/queue/notifications", notificationDTO);
  }

  public void createAndSendNotification(
      String senderId, String receiverId, NotificationType type, String message, String link) {
    Notification notification =
        Notification.builder()
            .sender(senderId)
            .receiver(receiverId)
            .type(type)
            .message(message)
            .link(link)
            .timestamp(LocalDateTime.now())
            .isRead(false)
            .build();

    Notification savedNotification = saveNotification(notification);

    List<User> users = userService.findAllById(List.of(senderId, receiverId));
    Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
    NotificationDto notificationDTO =
        notificationMapper.toNotificationDTO(savedNotification, userMap);
    sendNotification(receiverId, notificationDTO);
  }

  // Overload for backward compatibility
  public void createAndSendNotification(String senderId, String receiverId, NotificationType type) {
    createAndSendNotification(senderId, receiverId, type, generateMessage(senderId, type), null);
  }

  private void handleFollowOrLikeNotification(
      String senderId, String receiverId, NotificationType type) {
    Notification existing =
        notificationRepository
            .findTopBySenderAndReceiverAndTypeOrderByTimestampDesc(senderId, receiverId, type)
            .orElse(null);

    if (existing != null) {
      if (type == NotificationType.FOLLOW) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastSentTime = existing.getTimestamp();

        if (lastSentTime.isAfter(now.minusMinutes(1))) {
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
                }
              })
          .start();
    } else {
      sendNewNotification(senderId, receiverId, type);
    }
  }

  private void sendNewNotification(String senderId, String receiverId, NotificationType type) {
    createAndSendNotification(senderId, receiverId, type, generateMessage(senderId, type), null);
  }

  public List<NotificationDto> getNotificationsForUser(String receiverId) {
    List<Notification> notifications =
        notificationRepository.findByReceiverOrderByTimestampDesc(receiverId);
    List<User> users =
        userService.findAllById(
            notifications.stream()
                .map(Notification::getSender)
                .distinct()
                .collect(Collectors.toList()));

    Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));

    return notifications.stream()
        .map(notification -> notificationMapper.toNotificationDTO(notification, userMap))
        .collect(Collectors.toList());
  }

  public void markAllAsRead(String receiverId) {
    List<Notification> notifications =
        notificationRepository.findByReceiverOrderByTimestampDesc(receiverId);
    notifications.forEach(notification -> notification.setRead(true));
    notificationRepository.saveAll(notifications);
  }

  public void markAsRead(String notificationId, String receiverId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

    // Verify the notification belongs to the user
    if (!notification.getReceiver().equals(receiverId)) {
      throw new IllegalArgumentException("Notification does not belong to user");
    }

    notification.setRead(true);
    notificationRepository.save(notification);
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
}
