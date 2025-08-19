package com.unify.app.reports.domain.models;

import com.unify.app.users.domain.models.UserDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportWithReportersDto {
  String id;
  String reportedId;
  LocalDateTime reportedAt;
  EntityType entityType;
  Integer status;
  String reason;
  String adminReason;
  Object reportedEntity;
  private List<ReportImageDto> images;
  private List<UserDto> reporters;
}
