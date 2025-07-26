package com.unify.app.groups.domain;

import com.unify.app.groups.domain.models.GroupDto;
import com.unify.app.groups.domain.models.GroupMemberDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GroupMapper {
  GroupDto toGroupDTO(Group group);

  Group toGroupEntity(GroupDto dto);

  GroupMemberDto toGroupMemberDTO(GroupMember member);
}
