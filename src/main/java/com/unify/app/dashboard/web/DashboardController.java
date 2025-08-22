package com.unify.app.dashboard.web;

import com.unify.app.dashboard.domain.DashboardService;
import com.unify.app.dashboard.domain.models.DashboardStatsDto;
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
}
