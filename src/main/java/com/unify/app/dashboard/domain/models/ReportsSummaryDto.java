package com.unify.app.dashboard.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportsSummaryDto {
  private Long totalPendingReports;
  private ReportsByTypeDto reportsByType;
  private Long newReportsToday;
  private Long resolvedReportsToday;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ReportsByTypeDto {
    private ReportTypeCountDto posts;
    private ReportTypeCountDto users;
    private ReportTypeCountDto comments;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ReportTypeCountDto {
    private Long count;
  }
}
