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

  // Difference in entities from last month
  private Long userDifference;
  private Long postDifference;
  private Long activeUserDifference;

  private Long newReportsToday;
}
