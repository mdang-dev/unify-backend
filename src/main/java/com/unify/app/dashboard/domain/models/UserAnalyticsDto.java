package com.unify.app.dashboard.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalyticsDto {
  private String day;
  private String month;
  private Long newUsers;
  private Long activeUsers;
}
