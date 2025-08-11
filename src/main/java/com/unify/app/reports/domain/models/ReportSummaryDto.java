package com.unify.app.reports.domain.models;

import java.time.LocalDateTime;

public record ReportSummaryDto(
    String reportedId, // target id (postId or userId)
    EntityType entityType, // POST or USER
    Long reportCount, // number of reports for this target
    LocalDateTime latestReportedAt, // MAX(reportedAt)
    Integer sampleStatus, // status from the most recent report
    String displayLabel // Post caption snippet or User display name/handle
    ) {
  // Constructor without displayLabel for queries that don't include it
  public ReportSummaryDto(
      String reportedId,
      EntityType entityType,
      Long reportCount,
      LocalDateTime latestReportedAt,
      Integer sampleStatus) {
    this(reportedId, entityType, reportCount, latestReportedAt, sampleStatus, null);
  }
}
