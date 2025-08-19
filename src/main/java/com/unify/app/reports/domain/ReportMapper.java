package com.unify.app.reports.domain;

import com.unify.app.reports.domain.models.ReportDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {ReportImageMapper.class})
public interface ReportMapper {

  ReportDto toReportDTO(Report report);
}
