package com.unify.app.media.domain;

import com.unify.app.media.domain.models.StreamDto;
import com.unify.app.media.domain.models.StreamSimpleDto;
import com.unify.app.users.domain.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StreamMapper {

  StreamDto toDto(Stream entity);

  Stream toEntity(StreamDto dto);

  StreamSimpleDto toSimpleDto(Stream entity);

}
