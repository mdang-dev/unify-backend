package com.unify.app.users.web;

import com.unify.app.users.domain.UserService;
import com.unify.app.users.domain.models.ShareAbleUserDto;
import com.unify.app.users.domain.models.UserDto;
import com.unify.app.users.domain.models.auth.CreateUserCmd;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {

  private final UserService userService;

  // @GetMapping
  // List<UserReportCountDto> getUsers() {
  // return userService.findAllUserReportCount();
  // }

  @GetMapping("/my-info")
  UserDto getMyInfo() {
    return userService.getMyInfo();
  }

  @GetMapping("/username/{username}")
  UserDto getUserByUsername(@PathVariable String username) {
    return userService.findByUsername(username);
  }

  @GetMapping("/{id}")
  UserDto getUser(@PathVariable String id) {
    return userService.findById(id);
  }

  @GetMapping("/suggestions")
  ResponseEntity<List<UserDto>> getSuggestedUsers(@RequestParam String currentUserId) {
    List<UserDto> users = userService.getSuggestedUsers(currentUserId);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/follower")
  ResponseEntity<List<UserDto>> findUsersFollowingMe(@RequestParam String currentUserId) {
    List<UserDto> users = userService.findUsersFollowingMe(currentUserId);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/following")
  ResponseEntity<List<UserDto>> findUsersFollowedBy(@RequestParam String currentUserId) {
    List<UserDto> users = userService.findUsersFollowedBy(currentUserId);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/friend")
  ResponseEntity<List<UserDto>> getFriends(@RequestParam String currentUserId) {
    List<UserDto> friends = userService.getFriends(currentUserId);
    return ResponseEntity.ok(friends);
  }

  @GetMapping("/mutual")
  ResponseEntity<List<ShareAbleUserDto>> getMutualFollowers(@RequestParam String userId) {
    List<ShareAbleUserDto> users = userService.getMutualFollowers(userId);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/search")
  ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
    try {
      if (query == null || query.trim().isEmpty()) {
        return ResponseEntity.badRequest().build();
      }
      List<UserDto> users = userService.searchUsers(query.trim());
      return ResponseEntity.ok(users);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
    }
  }

  @PostMapping
  UserDto createUser(@RequestBody CreateUserCmd cmd) {
    return userService.createUser(cmd);
  }

  @PutMapping
  ResponseEntity<?> updateUser(@RequestBody UserDto userDto) {
    UserDto updatedUser = userService.updateUser(userDto);
    return ResponseEntity.ok(updatedUser);
  }

  @PutMapping("/permDisable/{id}")
  ResponseEntity<?> permDisableUser(@PathVariable String id) {
    userService.permanentlyDisableUser(id);
    return ResponseEntity.ok("Permanently disable success");
  }

  @PutMapping("/tempDisable/{id}")
  ResponseEntity<?> termDisableUser(@PathVariable String id) {
    userService.temporarilyDisableUser(id);
    return ResponseEntity.ok("Temporarily disable success");
  }

  @PutMapping("/unlock/{id}")
  ResponseEntity<?> unlockUser(@PathVariable String id) {
    userService.unlockUser(id);
    return ResponseEntity.ok("Unlock success");
  }

  @DeleteMapping("/{id}")
  ResponseEntity<String> removeUser(@PathVariable String id) {
    userService.removeUser(id);
    return ResponseEntity.ok("Remove User Successfully !");
  }
}
