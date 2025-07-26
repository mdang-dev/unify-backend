package com.unify.app.reports.domain;

import com.unify.app.reports.domain.models.ReportDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {

  @Mapping(target = "user", ignore = true)
  @Mapping(target = "entityType", ignore = true)
  Report toReport(ReportDto reportDTO);

  @Mapping(target = "reportedEntity", ignore = true)
  ReportDto toReportDTO(Report report);
}
