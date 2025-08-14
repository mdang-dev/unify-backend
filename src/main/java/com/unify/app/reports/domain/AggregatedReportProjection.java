package com.unify.app.reports.domain;

import com.unify.app.reports.domain.models.EntityType;

public interface AggregatedReportProjection {
  String getReportedId();

  EntityType getEntityType();

  Long getReportCount();

  Integer getMaxStatus();
}
