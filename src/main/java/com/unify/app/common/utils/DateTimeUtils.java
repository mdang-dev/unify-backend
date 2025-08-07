package com.unify.app.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeUtils {

  private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  /**
   * Lấy thời gian hiện tại theo timezone Việt Nam
   *
   * @return LocalDateTime theo timezone Việt Nam
   */
  public static LocalDateTime nowVietnam() {
    try {
      return ZonedDateTime.now(VIETNAM_ZONE).toLocalDateTime();
    } catch (Exception e) {
      // Fallback: return current time in system default timezone
      return LocalDateTime.now();
    }
  }

  /**
   * Chuyển đổi LocalDateTime sang timezone Việt Nam
   *
   * @param dateTime LocalDateTime cần chuyển đổi
   * @return LocalDateTime theo timezone Việt Nam
   */
  public static LocalDateTime toVietnamTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    try {
      return dateTime
          .atZone(ZoneId.systemDefault())
          .withZoneSameInstant(VIETNAM_ZONE)
          .toLocalDateTime();
    } catch (Exception e) {
      // Fallback: return the original dateTime if conversion fails
      return dateTime;
    }
  }
}
