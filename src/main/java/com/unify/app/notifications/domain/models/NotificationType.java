package com.unify.app.notifications.domain.models;

// Enum cho type
public enum NotificationType {
  POST,
  FOLLOW,
  MESSAGE,
  SYSTEM,
  LIKE,
  COMMENT,
  TAG,
  SHARE,
  REPORT,
  // Report-specific types
  POST_REPORT,
  COMMENT_REPORT,
  USER_REPORT,
  REPORT_APPROVED,
  ACCOUNT_SUSPENDED,
  ACCOUNT_BANNED;

  /**
   * Generate appropriate message for report types
   *
   * @param reportType The type of report (post, comment, user, etc.)
   * @return Appropriate message for the report type
   */
  public static String getReportMessage(String reportType) {
    return switch (reportType.toLowerCase()) {
      case "post" -> "Your post has been reported and approved.";
      case "comment" -> "Your comment has been reported and approved.";
      case "user" -> "Your profile has been reported and approved.";
      case "story" -> "Your story has been reported and approved.";
      case "reel" -> "Your reel has been reported and approved.";
      case "message" -> "Your message has been reported and approved.";
      default -> "Your content has been reported and approved.";
    };
  }
}
