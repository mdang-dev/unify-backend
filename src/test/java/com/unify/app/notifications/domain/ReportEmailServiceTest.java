package com.unify.app.notifications.domain;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.unify.app.reports.domain.models.EntityType;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.mail.ApacheMailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReportEmailServiceTest {

  @Mock private ApacheMailService apacheMailService;

  private ReportEmailService reportEmailService;

  @BeforeEach
  void setUp() {
    reportEmailService = new ReportEmailService(apacheMailService);
    ReflectionTestUtils.setField(reportEmailService, "frontendUrl", "http://localhost:3000");
  }

  @Test
  void sendReportApprovedEmail_ShouldSendEmailSuccessfully() {
    // Arrange
    User user = createTestUser();
    EntityType entityType = EntityType.POST;
    String entityId = "test-entity-id";
    String adminReason = "Violation of community guidelines";

    // Act
    reportEmailService.sendReportApprovedEmail(user, entityType, entityId, adminReason);

    // Assert
    verify(apacheMailService, times(1))
        .sendReportNotificationEmail(
            eq("test@example.com"), eq("Post Report Approved - Unify"), anyString());
  }

  @Test
  void sendAccountSuspendedEmail_ShouldSendEmailSuccessfully() {
    // Arrange
    User user = createTestUser();
    int reportCount = 3;

    // Act
    reportEmailService.sendAccountSuspendedEmail(user, reportCount);

    // Assert
    verify(apacheMailService, times(1))
        .sendReportNotificationEmail(
            eq("test@example.com"), eq("Account Temporarily Suspended - Unify"), anyString());
  }

  @Test
  void sendAccountBannedEmail_ShouldSendEmailSuccessfully() {
    // Arrange
    User user = createTestUser();
    int reportCount = 5;

    // Act
    reportEmailService.sendAccountBannedEmail(user, reportCount);

    // Assert
    verify(apacheMailService, times(1))
        .sendReportNotificationEmail(
            eq("test@example.com"), eq("Account Permanently Banned - Unify"), anyString());
  }

  @Test
  void sendReportApprovedEmail_WithNullAdminReason_ShouldHandleGracefully() {
    // Arrange
    User user = createTestUser();
    EntityType entityType = EntityType.COMMENT;
    String entityId = "test-comment-id";
    String adminReason = null;

    // Act
    reportEmailService.sendReportApprovedEmail(user, entityType, entityId, adminReason);

    // Assert
    verify(apacheMailService, times(1))
        .sendReportNotificationEmail(
            eq("test@example.com"), eq("Comment Report Approved - Unify"), anyString());
  }

  private User createTestUser() {
    return User.builder()
        .id("test-user-id")
        .firstName("John")
        .lastName("Doe")
        .email("test@example.com")
        .username("johndoe")
        .build();
  }
}
