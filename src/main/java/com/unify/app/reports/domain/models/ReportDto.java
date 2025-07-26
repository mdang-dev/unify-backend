package com.unify.app.reports.domain.models;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDto {
  String id;
  String reportedId;
  LocalDateTime reportedAt;
  EntityType entityType;
  Integer status;
  String reason;
  String adminReason;
  Object reportedEntity;
}
