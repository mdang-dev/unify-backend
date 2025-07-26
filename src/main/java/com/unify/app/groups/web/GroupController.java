package com.unify.app.groups.web;

import com.unify.app.groups.domain.GroupService;
import com.unify.app.groups.domain.models.GroupDto;
import com.unify.app.groups.domain.models.GroupMemberDto;
import com.unify.app.groups.domain.models.GroupMembershipResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
class GroupController {

  private final GroupService groupService;

  @PostMapping
  public GroupDto createGroup(@RequestBody GroupDto groupDTO, @RequestParam String ownerId) {
    return groupService.createGroup(groupDTO, ownerId);
  }

  @PutMapping("/{groupId}")
  public GroupDto updateGroup(@PathVariable String groupId, @RequestBody GroupDto groupDTO) {
    return groupService.updateGroup(groupId, groupDTO);
  }

  @DeleteMapping("/{groupId}")
  public void deleteGroup(@PathVariable String groupId) {
    groupService.deleteGroup(groupId);
  }

  @GetMapping("/{groupId}")
  public GroupDto getGroup(@PathVariable String groupId) {
    return groupService.getGroup(groupId);
  }

  @GetMapping
  public List<GroupDto> getAllGroups() {
    return groupService.getAllGroups();
  }

  @PostMapping("/{groupId}/join")
  public GroupMemberDto joinGroup(@PathVariable String groupId, @RequestParam String userId) {
    return groupService.joinGroup(groupId, userId);
  }

  @GetMapping("/my-groups")
  public List<GroupDto> getMyGroups() {
    return groupService.getGroupsByCurrentUser();
  }

  @PostMapping("/{groupId}/leave")
  public void leaveGroup(@PathVariable String groupId) {
    groupService.leaveGroup(groupId);
  }

  @GetMapping("/{groupId}/membership")
  public GroupMembershipResponse checkMembership(@PathVariable String groupId) {
    return groupService.checkMembership(groupId);
  }

  @GetMapping("/{groupId}/members")
  public List<GroupMemberDto> getGroupMembers(@PathVariable String groupId) {
    return groupService.getGroupMembers(groupId);
  }
}
