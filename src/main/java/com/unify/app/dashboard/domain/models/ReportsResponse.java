package com.unify.app.dashboard.domain.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportsResponse {
  private List<ReportedItemDto> data;
  private Long total;
  private Long pendingCount;
}
