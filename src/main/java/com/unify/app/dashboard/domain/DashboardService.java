package com.unify.app.dashboard.domain;

import com.unify.app.dashboard.domain.models.DashboardStatsDto;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    // Calculate growth percentages using formula: [(b)/(a)]*100
    Double userGrowthPercent = calculateGrowthPercent(totalUsersLastMonth, newUsersThisMonth);
    Double postGrowthPercent = calculateGrowthPercent(totalPostsLastMonth, newPostsThisMonth);
    Double activeUserGrowthPercent =
        calculateGrowthPercent(activeUsersLastMonth, newActiveUsersThisMonth);

    DashboardStatsDto stats = new DashboardStatsDto();
    stats.setTotalUsers(totalUsers);
    stats.setTotalPosts(totalPosts);
    stats.setTotalPendingReports(totalPendingReports);
    stats.setActiveUsers(activeUsers);
    stats.setUserGrowthPercent(userGrowthPercent);
    stats.setPostGrowthPercent(postGrowthPercent);
    stats.setActiveUserGrowthPercent(activeUserGrowthPercent);
    stats.setNewReportsToday(newReportsToday);
    return stats;
  }

  private Double calculateGrowthPercent(Long lastMonthValue, Long currentValue) {
    if (lastMonthValue == null || lastMonthValue == 0) {
      return currentValue > 0 ? 100.0 : 0.0;
    }

    double growth = ((double) (currentValue) / (lastMonthValue + currentValue)) * 100;
    return Math.round(growth * 10.0) / 10.0; // Round to 1 decimal place
  }
}
