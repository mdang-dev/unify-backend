package com.unify.app.notifications.domain;

import com.unify.app.reports.domain.models.EntityType;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.mail.ApacheMailService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications related to report actions Handles sending emails when
 * reports are approved by admins
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportEmailService {

  private final ApacheMailService apacheMailService;

  @Value("${unify.app-uri:http://localhost:3000}")
  private String frontendUrl;

  /**
   * Send email notification when a report is approved
   *
   * @param reportedUser The user who was reported
   * @param entityType Type of entity being reported (post, comment, user)
   * @param entityId ID of the reported entity
   * @param adminReason Reason provided by admin for approval
   */
  @Async
  public void sendReportApprovedEmail(
      User reportedUser, EntityType entityType, String entityId, String adminReason) {
    try {
      String subject = generateReportApprovedSubject(entityType);
      String emailContent =
          generateReportApprovedEmailContent(reportedUser, entityType, entityId, adminReason);

      // Send email using existing ApacheMailService
      apacheMailService.sendReportNotificationEmail(reportedUser.getEmail(), subject, emailContent);

      log.info(
          "Report approved email sent successfully to user: {}, entityType: {}, entityId: {}",
          reportedUser.getEmail(),
          entityType,
          entityId);

    } catch (Exception e) {
      log.error(
          "Failed to send report approved email to user: {}, entityType: {}, entityId: {}. Error: {}",
          reportedUser.getEmail(),
          entityType,
          entityId,
          e.getMessage(),
          e);
      // Don't throw exception to avoid breaking the main flow
    }
  }

  /**
   * Send email notification when account is suspended
   *
   * @param reportedUser The user whose account was suspended
   * @param reportCount Current report count
   */
  @Async
  public void sendAccountSuspendedEmail(User reportedUser, int reportCount) {
    try {
      String subject = "Account Temporarily Suspended - Unify";
      String emailContent = generateAccountSuspendedEmailContent(reportedUser, reportCount);

      apacheMailService.sendReportNotificationEmail(reportedUser.getEmail(), subject, emailContent);

      log.info(
          "Account suspended email sent successfully to user: {}, reportCount: {}",
          reportedUser.getEmail(),
          reportCount);

    } catch (Exception e) {
      log.error(
          "Failed to send account suspended email to user: {}, reportCount: {}. Error: {}",
          reportedUser.getEmail(),
          reportCount,
          e.getMessage(),
          e);
    }
  }

  /**
   * Send email notification when account is permanently banned
   *
   * @param reportedUser The user whose account was banned
   * @param reportCount Current report count
   */
  @Async
  public void sendAccountBannedEmail(User reportedUser, int reportCount) {
    try {
      String subject = "Account Permanently Banned - Unify";
      String emailContent = generateAccountBannedEmailContent(reportedUser, reportCount);

      apacheMailService.sendReportNotificationEmail(reportedUser.getEmail(), subject, emailContent);

      log.info(
          "Account banned email sent successfully to user: {}, reportCount: {}",
          reportedUser.getEmail(),
          reportCount);

    } catch (Exception e) {
      log.error(
          "Failed to send account banned email to user: {}, reportCount: {}. Error: {}",
          reportedUser.getEmail(),
          reportCount,
          e.getMessage(),
          e);
    }
  }

  private String generateReportApprovedSubject(EntityType entityType) {
    return switch (entityType) {
      case POST -> "Post Report Approved - Unify";
      case COMMENT -> "Comment Report Approved - Unify";
      case USER -> "User Report Approved - Unify";
      default -> "Report Approved - Unify";
    };
  }

  private String generateReportApprovedEmailContent(
      User reportedUser, EntityType entityType, String entityId, String adminReason) {
    Map<String, String> templateData = new HashMap<>();
    templateData.put("USER_NAME", reportedUser.getFirstName() + " " + reportedUser.getLastName());
    templateData.put("ENTITY_TYPE", entityType.name().toLowerCase());
    templateData.put("ENTITY_ID", entityId);
    templateData.put(
        "ADMIN_REASON", adminReason != null ? adminReason : "No specific reason provided");
    templateData.put("FRONTEND_URL", frontendUrl);
    templateData.put("YEAR", String.valueOf(java.time.Year.now().getValue()));

    // Load and populate the appropriate email template
    String template = loadReportApprovedEmailTemplate(entityType);
    return populateTemplate(template, templateData);
  }

  private String generateAccountSuspendedEmailContent(User reportedUser, int reportCount) {
    Map<String, String> templateData = new HashMap<>();
    templateData.put("USER_NAME", reportedUser.getFirstName() + " " + reportedUser.getLastName());
    templateData.put("REPORT_COUNT", String.valueOf(reportCount));
    templateData.put("FRONTEND_URL", frontendUrl);
    templateData.put("YEAR", String.valueOf(java.time.Year.now().getValue()));

    String template = loadAccountSuspendedEmailTemplate();
    return populateTemplate(template, templateData);
  }

  private String generateAccountBannedEmailContent(User reportedUser, int reportCount) {
    Map<String, String> templateData = new HashMap<>();
    templateData.put("USER_NAME", reportedUser.getFirstName() + " " + reportedUser.getLastName());
    templateData.put("REPORT_COUNT", String.valueOf(reportCount));
    templateData.put("FRONTEND_URL", frontendUrl);
    templateData.put("YEAR", String.valueOf(java.time.Year.now().getValue()));

    String template = loadAccountBannedEmailTemplate();
    return populateTemplate(template, templateData);
  }

  private String loadReportApprovedEmailTemplate(EntityType entityType) {
    // For now, return a simple inline template
    // In production, you might want to load from external files
    return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>Report Approved</title>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f6f8fa; margin: 0; padding: 0; }
                    .container { max-width: 520px; margin: 24px auto; background: #ffffff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06); overflow: hidden; }
                    .header { background: #dc3545; color: #ffffff; padding: 16px 24px; font-size: 18px; font-weight: 600; }
                    .content { padding: 24px; color: #24292e; }
                    .warning { background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 16px; margin: 16px 0; }
                    .footer { padding: 16px 24px; color: #6a737d; font-size: 12px; border-top: 1px solid #f0f1f3; }
                    .button { display: inline-block; background: #0d6efd; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin: 16px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">Report Approved - Action Required</div>
                    <div class="content">
                        <p>Dear ${USER_NAME},</p>
                        <p>A report regarding your ${ENTITY_TYPE} has been reviewed and approved by our moderation team.</p>

                        <div class="warning">
                            <strong>Admin Reason:</strong><br>
                            ${ADMIN_REASON}
                        </div>

                        <p>Please review your content and ensure it complies with our community guidelines. You can access your account at:</p>

                        <a href="${FRONTEND_URL}" class="button">Access Platform</a>

                        <p>If you have any questions or believe this decision was made in error, please contact our support team.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; ${YEAR} Unify. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
  }

  private String loadAccountSuspendedEmailTemplate() {
    return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>Account Suspended</title>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f6f8fa; margin: 0; padding: 0; }
                    .container { max-width: 520px; margin: 24px auto; background: #ffffff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06); overflow: hidden; }
                    .header { background: #ffc107; color: #000000; padding: 16px 24px; font-size: 18px; font-weight: 600; }
                    .content { padding: 24px; color: #24292e; }
                    .warning { background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 16px; margin: 16px 0; }
                    .footer { padding: 16px 24px; color: #6a737d; font-size: 12px; border-top: 1px solid #f0f1f3; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">Account Temporarily Suspended</div>
                    <div class="content">
                        <p>Dear ${USER_NAME},</p>
                        <p>Your account has been temporarily suspended due to ${REPORT_COUNT} approved reports.</p>

                        <div class="warning">
                            <strong>What this means:</strong><br>
                            • Your account is temporarily inaccessible<br>
                            • This suspension will be reviewed by our team<br>
                            • You may appeal this decision
                        </div>

                        <p>Please review our community guidelines and ensure future content complies with our standards.</p>

                        <p>If you have any questions or wish to appeal this suspension, please contact our support team.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; ${YEAR} Unify. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
  }

  private String loadAccountBannedEmailTemplate() {
    return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>Account Banned</title>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f6f8fa; margin: 0; padding: 0; }
                    .container { max-width: 520px; margin: 24px auto; background: #ffffff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06); overflow: hidden; }
                    .header { background: #dc3545; color: #ffffff; padding: 16px 24px; font-size: 18px; font-weight: 600; }
                    .content { padding: 24px; color: #24292e; }
                    .warning { background: #f8d7da; border: 1px solid #f5c6cb; border-radius: 8px; padding: 16px; margin: 16px 0; }
                    .footer { padding: 16px 24px; color: #6a737d; font-size: 12px; border-top: 1px solid #f0f1f3; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">Account Permanently Banned</div>
                    <div class="content">
                        <p>Dear ${USER_NAME},</p>
                        <p>Your account has been permanently banned due to ${REPORT_COUNT} approved reports.</p>

                        <div class="warning">
                            <strong>What this means:</strong><br>
                            • Your account is permanently inaccessible<br>
                            • All content has been removed<br>
                            • This decision is final
                        </div>

                        <p>This action was taken after multiple violations of our community guidelines. The decision is final and cannot be appealed.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; ${YEAR} Unify. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
  }

  private String populateTemplate(String template, Map<String, String> data) {
    String result = template;
    for (Map.Entry<String, String> entry : data.entrySet()) {
      result = result.replace("${" + entry.getKey() + "}", entry.getValue());
    }
    return result;
  }
}
