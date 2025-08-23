package com.unify.app.notifications.domain;

import com.unify.app.notifications.domain.models.NotificationType;
import com.unify.app.reports.domain.models.EntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Specialized service for handling report-related notifications Provides type-specific notification
 * creation and message generation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportNotificationService {

  private final NotificationService notificationService;

  /**
   * Send notification when a report is approved
   *
   * @param reportedUserId ID of the user being reported
   * @param entityType Type of entity being reported (post, comment, user, etc.)
   * @param entityId ID of the reported entity
   * @param adminId ID of the admin who approved the report
   */
  public void sendReportApprovedNotification(
      String reportedUserId,
      EntityType entityType,
      String entityId,
      String adminId,
      String adminReason) {
    try {
      // Determine the specific notification type based on entity type
      NotificationType notificationType = getNotificationTypeForEntity(entityType);

      // Generate appropriate message for the entity type
      String message = NotificationType.getReportMessage(entityType.name().toLowerCase());

      // Create notification data with entity information and admin reason
      String notificationData =
          String.format(
              "{\"reportType\":\"%s\",\"entityId\":\"%s\",\"adminId\":\"%s\",\"adminReason\":\"%s\"}",
              entityType.name().toLowerCase(),
              entityId,
              adminId,
              adminReason != null ? adminReason : "");

      // Send the notification
      notificationService.createAndSendNotification(
          adminId, // Admin who approved the report
          reportedUserId, // User being reported
          notificationType,
          message,
          generateLinkForEntity(entityType, entityId),
          notificationData);

      log.info(
          "Report approved notification sent: type={}, entityId={}, reportedUser={}, adminReason={}",
          entityType,
          entityId,
          reportedUserId,
          adminReason);

    } catch (Exception e) {
      log.error("Failed to send report approved notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Send notification when account is suspended due to reports
   *
   * @param userId ID of the suspended user
   * @param reportCount Current report count
   * @param adminId ID of the admin who took action
   */
  public void sendAccountSuspendedNotification(String userId, int reportCount, String adminId) {
    try {
      String message =
          String.format(
              "Your account has been temporarily suspended due to %d approved reports.",
              reportCount);

      String notificationData =
          String.format(
              "{\"action\":\"suspension\",\"reportCount\":%d,\"adminId\":\"%s\"}",
              reportCount, adminId);

      notificationService.createAndSendNotification(
          adminId, userId, NotificationType.ACCOUNT_SUSPENDED, message, "/login", notificationData);

      log.info(
          "Account suspended notification sent: userId={}, reportCount={}", userId, reportCount);

    } catch (Exception e) {
      log.error("Failed to send account suspended notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Send notification when account is banned due to reports
   *
   * @param userId ID of the banned user
   * @param reportCount Current report count
   * @param adminId ID of the admin who took action
   */
  public void sendAccountBannedNotification(String userId, int reportCount, String adminId) {
    try {
      String message =
          String.format(
              "Your account has been permanently banned due to %d approved reports.", reportCount);

      String notificationData =
          String.format(
              "{\"action\":\"ban\",\"reportCount\":%d,\"adminId\":\"%s\"}", reportCount, adminId);

      notificationService.createAndSendNotification(
          adminId, userId, NotificationType.ACCOUNT_BANNED, message, "/login", notificationData);

      log.info("Account banned notification sent: userId={}, reportCount={}", userId, reportCount);

    } catch (Exception e) {
      log.error("Failed to send account banned notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Map entity type to notification type
   *
   * @param entityType The type of entity being reported
   * @return Corresponding notification type
   */
  private NotificationType getNotificationTypeForEntity(EntityType entityType) {
    return switch (entityType) {
      case POST -> NotificationType.POST_REPORT;
      case COMMENT -> NotificationType.COMMENT_REPORT;
      case USER -> NotificationType.USER_REPORT;
      default -> NotificationType.REPORT_APPROVED;
    };
  }

  /**
   * Generate appropriate link for the reported entity
   *
   * @param entityType Type of entity
   * @param entityId ID of entity
   * @return Link to view the reported content
   */
  private String generateLinkForEntity(EntityType entityType, String entityId) {
    return switch (entityType) {
      case POST -> "/posts/" + entityId;
      case COMMENT -> "/posts/" + entityId + "#comment-" + entityId;
      case USER -> "/profile/" + entityId;
      default -> "/settings";
    };
  }
}
