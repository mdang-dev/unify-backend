package com.unify.app.groups.domain;

import com.unify.app.groups.domain.models.GroupDto;
import com.unify.app.groups.domain.models.GroupMemberDto;
import com.unify.app.groups.domain.models.GroupMemberRole;
import com.unify.app.groups.domain.models.GroupMembershipResponse;
import com.unify.app.groups.domain.models.GroupStatus;
import com.unify.app.groups.domain.models.PrivacyType;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final UserService userService;
  private final GroupMapper groupMapper;

  @Transactional
  public GroupDto createGroup(GroupDto groupDTO, String ownerId) {
    Group group = groupMapper.toGroupEntity(groupDTO);
    LocalDateTime now = LocalDateTime.now();
    group.setCreatedAt(now);
    group.setUpdatedAt(now);
    // Set default values if not provided
    if (group.getPrivacyType() == null) {
      group.setPrivacyType(PrivacyType.PUBLIC);
    }
    if (group.getStatus() == null) {
      group.setStatus(GroupStatus.ACTIVE);
    }
    group = groupRepository.save(group);
    User owner = userService.findUserById(ownerId);
    GroupMember member = new GroupMember();
    member.setGroup(group);
    member.setUser(owner);
    member.setJoinedAt(LocalDateTime.now());
    member.setRole(GroupMemberRole.OWNER);
    groupMemberRepository.save(member);
    return groupMapper.toGroupDTO(group);
  }

  public GroupDto updateGroup(String groupId, GroupDto groupDTO) {
    Group group =
        groupRepository.findById(groupId).orElseThrow(() -> GroupNotFoundException.forNotFound());
    group.setName(groupDTO.name());
    group.setPrivacyType(groupDTO.privacyType());
    group.setDescription(groupDTO.description());
    group.setCoverImageUrl(groupDTO.coverImageUrl());
    group.setStatus(groupDTO.status());
    group = groupRepository.save(group);
    return groupMapper.toGroupDTO(group);
  }

  public void deleteGroup(String groupId) {
    groupRepository.deleteById(groupId);
  }

  public GroupDto getGroup(String groupId) {
    Group group =
        groupRepository.findById(groupId).orElseThrow(() -> GroupNotFoundException.forNotFound());
    return groupMapper.toGroupDTO(group);
  }

  public Group findById(String id) {
    return groupRepository.findById(id).orElseThrow(() -> GroupNotFoundException.forNotFound());
  }

  public List<GroupDto> getAllGroups() {
    return groupRepository.findAll().stream()
        .map(groupMapper::toGroupDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public GroupMemberDto joinGroup(String groupId, String userId) {
    Group group = this.findById(groupId);
    User user = userService.findUserById(userId);
    GroupMember member = new GroupMember();
    member.setGroup(group);
    member.setUser(user);
    member.setJoinedAt(LocalDateTime.now());
    member.setRole(GroupMemberRole.MEMBER);
    member = groupMemberRepository.save(member);
    return groupMapper.toGroupMemberDTO(member);
  }

  public List<GroupDto> getGroupsByPrivacyType(PrivacyType privacyType) {
    return groupRepository.findByPrivacyType(privacyType).stream()
        .map(groupMapper::toGroupDTO)
        .collect(Collectors.toList());
  }

  public List<GroupDto> getGroupsByStatus(GroupStatus status) {
    return groupRepository.findByStatus(status).stream()
        .map(groupMapper::toGroupDTO)
        .collect(Collectors.toList());
  }

  public GroupDto updateGroupStatus(String groupId, GroupStatus status) {
    Group group = this.findById(groupId);
    group.setStatus(status);
    group.setUpdatedAt(LocalDateTime.now());
    group = groupRepository.save(group);
    return groupMapper.toGroupDTO(group);
  }

  public GroupDto updateGroupPrivacy(String groupId, PrivacyType privacyType) {
    Group group = this.findById(groupId);
    group.setPrivacyType(privacyType);
    group.setUpdatedAt(LocalDateTime.now());
    group = groupRepository.save(group);
    return groupMapper.toGroupDTO(group);
  }

  public List<GroupDto> getGroupsByCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new RuntimeException("User not authenticated");
    }

    String userEmail = authentication.getName();
    User user = userService.findByEmail(userEmail);

    List<GroupMember> groupMembers = groupMemberRepository.findActiveGroupsByUserId(user.getId());

    return groupMembers.stream()
        .map(groupMember -> groupMapper.toGroupDTO(groupMember.getGroup()))
        .collect(Collectors.toList());
  }

  @Transactional
  public void leaveGroup(String groupId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new RuntimeException("User not authenticated");
    }

    String userEmail = authentication.getName();
    User user = userService.findByEmail(userEmail);

    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));

    GroupMember member =
        groupMemberRepository
            .findByGroupAndUser(group, user)
            .orElseThrow(() -> new RuntimeException("User is not a member of this group"));

    groupMemberRepository.delete(member);
  }

  public GroupMembershipResponse checkMembership(String groupId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return new GroupMembershipResponse(false, false, null);
    }

    String userEmail = authentication.getName();
    User user = userService.findByEmail(userEmail);

    if (user == null) {
      return new GroupMembershipResponse(false, false, null);
    }

    Group group = groupRepository.findById(groupId).orElse(null);

    if (group == null) {
      return new GroupMembershipResponse(false, false, null);
    }

    GroupMember member = groupMemberRepository.findByGroupAndUser(group, user).orElse(null);

    if (member == null) {
      return new GroupMembershipResponse(false, false, null);
    }

    boolean isOwner = GroupMemberRole.OWNER.equals(member.getRole());

    return new GroupMembershipResponse(true, isOwner, member.getRole().toString());
  }

  public List<GroupMemberDto> getGroupMembers(String groupId) {
    Group group = this.findById(groupId);

    List<GroupMember> groupMembers = groupMemberRepository.findByGroup(group);

    return groupMembers.stream().map(groupMapper::toGroupMemberDTO).collect(Collectors.toList());
  }
}
