package com.unify.app.dashboard.web;

import com.unify.app.dashboard.domain.DashboardService;
import com.unify.app.dashboard.domain.models.DashboardStatsDto;
import com.unify.app.dashboard.domain.models.ReportsResponse;
import com.unify.app.dashboard.domain.models.ReportsSummaryDto;
import com.unify.app.dashboard.domain.models.UserAnalyticsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/stats")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<DashboardStatsDto> getDashboardStats() {
    DashboardStatsDto stats = dashboardService.getDashboardStats();
    return ResponseEntity.ok(stats);
  }

  @GetMapping("/analytics")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserAnalyticsResponse> getUserAnalytics(@RequestParam String period) {
    UserAnalyticsResponse analytics = dashboardService.getUserAnalytics(period);
    return ResponseEntity.ok(analytics);
  }

  // Reports endpoints
  @GetMapping("/reports/posts")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ReportsResponse> getReportedPosts() {
    ReportsResponse response = dashboardService.getReportedPosts();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/reports/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ReportsResponse> getReportedUsers() {
    ReportsResponse response = dashboardService.getReportedUsers();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/reports/comments")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ReportsResponse> getReportedComments() {
    ReportsResponse response = dashboardService.getReportedComments();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/reports/summary")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ReportsSummaryDto> getReportsSummary() {
    ReportsSummaryDto summary = dashboardService.getReportsSummary();
    return ResponseEntity.ok(summary);
  }
}
