package com.unify.app.dashboard.domain;

import com.unify.app.dashboard.domain.models.DashboardStatsDto;
import com.unify.app.dashboard.domain.models.ReportedItemDto;
import com.unify.app.dashboard.domain.models.ReportsResponse;
import com.unify.app.dashboard.domain.models.ReportsSummaryDto;
import com.unify.app.dashboard.domain.models.UserAnalyticsDto;
import com.unify.app.dashboard.domain.models.UserAnalyticsResponse;
import com.unify.app.reports.domain.models.EntityType;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DashboardService {

  private final DashboardRepository dashboardRepository;

  public DashboardService(DashboardRepository dashboardRepository) {
    this.dashboardRepository = dashboardRepository;
  }

  public DashboardStatsDto getDashboardStats() {
    // Calculate date ranges
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
    LocalDateTime currentMonthStart =
        now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    LocalDateTime lastMonthStart = currentMonthStart.minus(1, ChronoUnit.MONTHS);

    // Get current statistics
    Long totalUsers = dashboardRepository.getTotalUsers();
    Long totalPosts = dashboardRepository.getTotalPosts();
    Long totalPendingReports = dashboardRepository.getTotalPendingReports();
    Long activeUsers = dashboardRepository.getActiveUsers(thirtyDaysAgo);
    Long newReportsToday = dashboardRepository.getNewReportsToday();

    // Get last month statistics for growth calculation (a)
    Long totalUsersLastMonth =
        dashboardRepository.getTotalUsersLastMonth(lastMonthStart, currentMonthStart);
    Long totalPostsLastMonth =
        dashboardRepository.getTotalPostsLastMonth(lastMonthStart, currentMonthStart);
    Long activeUsersLastMonth =
        dashboardRepository.getActiveUsersLastMonth(lastMonthStart, currentMonthStart);

    // Get new entities this month for growth calculation (b)
    Long newUsersThisMonth = dashboardRepository.getNewUsersThisMonth(currentMonthStart);
    Long newPostsThisMonth = dashboardRepository.getNewPostsThisMonth(currentMonthStart);
    Long newActiveUsersThisMonth =
        dashboardRepository.getNewActiveUsersThisMonth(currentMonthStart);

    // Calculate growth percentages using formula: ((current - last) / last) * 100
    Double userGrowthPercent = calculateGrowthPercent(totalUsersLastMonth, newUsersThisMonth);
    Double postGrowthPercent = calculateGrowthPercent(totalPostsLastMonth, newPostsThisMonth);
    Double activeUserGrowthPercent =
        calculateGrowthPercent(activeUsersLastMonth, newActiveUsersThisMonth);

    // Calculate differences (current - last)
    Long userDifference = newUsersThisMonth - totalUsersLastMonth;
    Long postDifference = newPostsThisMonth - totalPostsLastMonth;
    Long activeUserDifference = newActiveUsersThisMonth - activeUsersLastMonth;

    return DashboardStatsDto.builder()
        .totalUsers(totalUsers)
        .totalPosts(totalPosts)
        .totalPendingReports(totalPendingReports)
        .activeUsers(activeUsers)
        .userGrowthPercent(userGrowthPercent)
        .postGrowthPercent(postGrowthPercent)
        .activeUserGrowthPercent(activeUserGrowthPercent)
        .userDifference(userDifference)
        .postDifference(postDifference)
        .activeUserDifference(activeUserDifference)
        .newReportsToday(newReportsToday)
        .build();
  }

  private Double calculateGrowthPercent(Long lastMonthValue, Long currentValue) {
    if (lastMonthValue == null || lastMonthValue == 0) {
      return currentValue > 0 ? 100.0 : 0.0;
    }

    double growth = ((double) (currentValue - lastMonthValue) / lastMonthValue) * 100;
    return Math.round(growth * 10.0) / 10.0; // Round to 1 decimal place
  }

  public UserAnalyticsResponse getUserAnalytics(String period) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startDate;
    LocalDateTime endDate;

    switch (period.toLowerCase()) {
      case "7days":
        // Current week: Sunday to Saturday
        // Calculate days since last Sunday
        int daysSinceSunday = now.getDayOfWeek().getValue() % 7;
        LocalDateTime sunday = now.minusDays(daysSinceSunday);
        LocalDateTime saturday = sunday.plusDays(6);
        startDate = sunday.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = saturday.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return getAnalyticsFor7Days(startDate, endDate);
      case "30days":
        // Current month: 1st to last day of month
        startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate =
            now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999);
        return getAnalyticsFor30Days(startDate, endDate);
      case "12months":
        // Current year: January 1st to December 31st
        startDate =
            now.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate =
            now.withMonth(12)
                .withDayOfMonth(31)
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999);
        return getAnalyticsFor12Months(startDate, endDate);
      default:
        throw new IllegalArgumentException(
            "Invalid period. Must be '7days', '30days', or '12months'");
    }
  }

  private UserAnalyticsResponse getAnalyticsFor7Days(
      LocalDateTime startDate, LocalDateTime endDate) {
    List<Object[]> newUsersData = dashboardRepository.getNewUsersByDay(startDate, endDate);
    List<Object[]> activeUsersData = dashboardRepository.getActiveUsersByDay(startDate, endDate);

    Map<String, Long> newUsersMap = new HashMap<>();
    Map<String, Long> activeUsersMap = new HashMap<>();

    // Process new users data
    for (Object[] row : newUsersData) {
      String date = row[0].toString();
      Long count = (Long) row[1];
      newUsersMap.put(date, count);
    }

    // Process active users data
    for (Object[] row : activeUsersData) {
      String date = row[0].toString();
      Long count = (Long) row[1];
      activeUsersMap.put(date, count);
    }

    List<UserAnalyticsDto> analyticsData = new ArrayList<>();

    // Generate data for Sunday to Saturday (7 days)
    for (int i = 0; i < 7; i++) {
      LocalDateTime currentDate = startDate.plus(i, ChronoUnit.DAYS);
      String dayName = currentDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
      String dateKey = currentDate.toLocalDate().toString();

      UserAnalyticsDto dto =
          UserAnalyticsDto.builder()
              .day(dayName)
              .newUsers(newUsersMap.getOrDefault(dateKey, 0L))
              .activeUsers(activeUsersMap.getOrDefault(dateKey, 0L))
              .build();
      analyticsData.add(dto);
    }

    return UserAnalyticsResponse.builder().data(analyticsData).build();
  }

  private UserAnalyticsResponse getAnalyticsFor30Days(
      LocalDateTime startDate, LocalDateTime endDate) {
    List<Object[]> newUsersData = dashboardRepository.getNewUsersByWeek(startDate, endDate);
    List<Object[]> activeUsersData = dashboardRepository.getActiveUsersByWeek(startDate, endDate);

    Map<Integer, Long> newUsersMap = new HashMap<>();
    Map<Integer, Long> activeUsersMap = new HashMap<>();

    // Process new users data
    for (Object[] row : newUsersData) {
      Integer week = (Integer) row[0];
      Long count = (Long) row[1];
      newUsersMap.put(week, count);
    }

    // Process active users data
    for (Object[] row : activeUsersData) {
      Integer week = (Integer) row[0];
      Long count = (Long) row[1];
      activeUsersMap.put(week, count);
    }

    List<UserAnalyticsDto> analyticsData = new ArrayList<>();

    // Calculate weeks dynamically based on the date range
    int startWeek = startDate.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
    int endWeek = endDate.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());

    // Handle year boundary
    int currentWeek = startWeek;
    int weekCount = 0;

    while (weekCount < 4 && currentWeek <= endWeek) {
      UserAnalyticsDto dto =
          UserAnalyticsDto.builder()
              .day("Week " + (weekCount + 1))
              .newUsers(newUsersMap.getOrDefault(currentWeek, 0L))
              .activeUsers(activeUsersMap.getOrDefault(currentWeek, 0L))
              .build();
      analyticsData.add(dto);

      currentWeek++;
      weekCount++;

      // Handle year boundary
      if (currentWeek > 53) {
        currentWeek = 1;
      }
    }

    return UserAnalyticsResponse.builder().data(analyticsData).build();
  }

  private UserAnalyticsResponse getAnalyticsFor12Months(
      LocalDateTime startDate, LocalDateTime endDate) {
    List<Object[]> newUsersData = dashboardRepository.getNewUsersByMonth(startDate, endDate);
    List<Object[]> activeUsersData = dashboardRepository.getActiveUsersByMonth(startDate, endDate);

    Map<Integer, Long> newUsersMap = new HashMap<>();
    Map<Integer, Long> activeUsersMap = new HashMap<>();

    // Process new users data
    for (Object[] row : newUsersData) {
      Integer month = (Integer) row[0];
      Long count = (Long) row[1];
      newUsersMap.put(month, count);
    }

    // Process active users data
    for (Object[] row : activeUsersData) {
      Integer month = (Integer) row[0];
      Long count = (Long) row[1];
      activeUsersMap.put(month, count);
    }

    List<UserAnalyticsDto> analyticsData = new ArrayList<>();
    String[] monthNames = {
      "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    // Calculate months dynamically based on the date range
    int startMonth = startDate.getMonthValue();
    int endMonth = endDate.getMonthValue();
    int startYear = startDate.getYear();
    int endYear = endDate.getYear();

    int currentMonth = startMonth;
    int currentYear = startYear;
    int monthCount = 0;

    while (monthCount < 12
        && (currentYear < endYear || (currentYear == endYear && currentMonth <= endMonth))) {
      UserAnalyticsDto dto =
          UserAnalyticsDto.builder()
              .month(monthNames[currentMonth - 1])
              .newUsers(newUsersMap.getOrDefault(currentMonth, 0L))
              .activeUsers(activeUsersMap.getOrDefault(currentMonth, 0L))
              .build();
      analyticsData.add(dto);

      currentMonth++;
      if (currentMonth > 12) {
        currentMonth = 1;
        currentYear++;
      }
      monthCount++;
    }

    return UserAnalyticsResponse.builder().data(analyticsData).build();
  }

  // Reports methods
  public ReportsResponse getReportedPosts() {
    List<Object[]> reportsData = dashboardRepository.getReportedPosts();
    List<ReportedItemDto> data = new ArrayList<>();

    // Limit to 4 records for quick review
    int limit = Math.min(4, reportsData.size());
    for (int i = 0; i < limit; i++) {
      Object[] row = reportsData.get(i);
      ReportedItemDto dto =
          ReportedItemDto.builder()
              .reportedId((String) row[0])
              .latestReportedAt((LocalDateTime) row[1])
              .reportCount((Long) row[2])
              .type("post")
              .postTitle((String) row[3])
              .authorName((String) row[4])
              .authorId((String) row[5])
              .build();
      data.add(dto);
    }

    // Get total and pending counts for posts specifically
    Long total = dashboardRepository.getTotalReportsByType(EntityType.POST);
    Long pendingCount = dashboardRepository.getPendingReportsByType(EntityType.POST);

    return ReportsResponse.builder().data(data).total(total).pendingCount(pendingCount).build();
  }

  public ReportsResponse getReportedUsers() {
    List<Object[]> reportsData = dashboardRepository.getReportedUsers();
    List<ReportedItemDto> data = new ArrayList<>();

    // Limit to 4 records for quick review
    int limit = Math.min(4, reportsData.size());
    for (int i = 0; i < limit; i++) {
      Object[] row = reportsData.get(i);
      ReportedItemDto dto =
          ReportedItemDto.builder()
              .reportedId((String) row[0])
              .latestReportedAt((LocalDateTime) row[1])
              .reportCount((Long) row[2])
              .type("user")
              .userName((String) row[3])
              .userEmail((String) row[4])
              .userAvatar((String) row[5])
              .build();
      data.add(dto);
    }

    // Get total and pending counts for users specifically
    Long total = dashboardRepository.getTotalReportsByType(EntityType.USER);
    Long pendingCount = dashboardRepository.getPendingReportsByType(EntityType.USER);

    return ReportsResponse.builder().data(data).total(total).pendingCount(pendingCount).build();
  }

  public ReportsResponse getReportedComments() {
    List<Object[]> reportsData = dashboardRepository.getReportedComments();
    List<ReportedItemDto> data = new ArrayList<>();

    // Limit to 4 records for quick review
    int limit = Math.min(4, reportsData.size());
    for (int i = 0; i < limit; i++) {
      Object[] row = reportsData.get(i);
      ReportedItemDto dto =
          ReportedItemDto.builder()
              .reportedId((String) row[0])
              .latestReportedAt((LocalDateTime) row[1])
              .reportCount((Long) row[2])
              .type("comment")
              .commentContent((String) row[3])
              .authorName((String) row[4])
              .authorId((String) row[5])
              .parentPostTitle((String) row[6])
              .build();
      data.add(dto);
    }

    // Get total and pending counts for comments specifically
    Long total = dashboardRepository.getTotalReportsByType(EntityType.COMMENT);
    Long pendingCount = dashboardRepository.getPendingReportsByType(EntityType.COMMENT);

    return ReportsResponse.builder().data(data).total(total).pendingCount(pendingCount).build();
  }

  public ReportsSummaryDto getReportsSummary() {
    Long totalPendingReports = dashboardRepository.getTotalPendingReports();
    List<Object[]> reportsByType = dashboardRepository.getPendingReportsByType();
    Long newReportsToday = dashboardRepository.getNewReportsToday();
    Long resolvedReportsToday = dashboardRepository.getResolvedReportsToday();

    // Build reports by type
    ReportsSummaryDto.ReportsByTypeDto reportsByTypeDto =
        ReportsSummaryDto.ReportsByTypeDto.builder()
            .posts(ReportsSummaryDto.ReportTypeCountDto.builder().count(0L).build())
            .users(ReportsSummaryDto.ReportTypeCountDto.builder().count(0L).build())
            .comments(ReportsSummaryDto.ReportTypeCountDto.builder().count(0L).build())
            .build();

    for (Object[] row : reportsByType) {
      EntityType entityType = (EntityType) row[0];
      Long count = (Long) row[1];

      switch (entityType) {
        case POST:
          reportsByTypeDto.setPosts(
              ReportsSummaryDto.ReportTypeCountDto.builder().count(count).build());
          break;
        case USER:
          reportsByTypeDto.setUsers(
              ReportsSummaryDto.ReportTypeCountDto.builder().count(count).build());
          break;
        case COMMENT:
          reportsByTypeDto.setComments(
              ReportsSummaryDto.ReportTypeCountDto.builder().count(count).build());
          break;
      }
    }

    return ReportsSummaryDto.builder()
        .totalPendingReports(totalPendingReports)
        .reportsByType(reportsByTypeDto)
        .newReportsToday(newReportsToday)
        .resolvedReportsToday(resolvedReportsToday)
        .build();
  }
}
