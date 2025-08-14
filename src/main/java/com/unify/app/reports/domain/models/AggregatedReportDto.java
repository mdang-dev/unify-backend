package com.unify.app.reports.domain.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedReportDto {
  private String reportedId;
  private EntityType entityType;
  private int reportCount;
  private int status;
  private List<String> reasons;
  private List<String> urls;
}
