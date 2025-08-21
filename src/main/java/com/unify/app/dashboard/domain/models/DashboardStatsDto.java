package com.unify.app.dashboard.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
  private Long totalUsers;
  private Long totalPosts;
  private Long totalPendingReports;
  private Long activeUsers;

  // Growth percentages
  private Double userGrowthPercent;
  private Double postGrowthPercent;
  private Double activeUserGrowthPercent;
  private Long newReportsToday;
}
