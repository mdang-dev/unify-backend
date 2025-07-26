package com.unify.app.messages.domain;

import com.unify.app.messages.domain.models.MessageDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {

  MessageDto toDto(Message message);

  Message toEntity(MessageDto dto);
}
