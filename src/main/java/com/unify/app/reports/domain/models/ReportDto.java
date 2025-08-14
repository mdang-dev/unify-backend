package com.unify.app.reports.domain.models;

import java.time.LocalDateTime;
import java.util.List;
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
  String userId;
  private List<ReportImageDto> images;
}
