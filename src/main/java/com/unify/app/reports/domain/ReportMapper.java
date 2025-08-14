package com.unify.app.reports.domain;

import com.unify.app.reports.domain.models.ReportDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {ReportImageMapper.class})
public interface ReportMapper {

  @Mapping(target = "user", ignore = true)
  @Mapping(target = "entityType", ignore = true)
  Report toReport(ReportDto reportDTO);

  @Mapping(target = "reportedEntity", ignore = true)
  @Mapping(source = "user.username", target = "userId")
  @Mapping(source = "images", target = "images")
  ReportDto toReportDTO(Report report);
}
