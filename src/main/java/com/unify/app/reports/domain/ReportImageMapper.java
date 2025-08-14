package com.unify.app.reports.domain;

import com.unify.app.reports.domain.models.ReportImageDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportImageMapper {
  ReportImageDto toDto(ReportImage entity);

  ReportImage toEntity(ReportImageDto dto);
}
