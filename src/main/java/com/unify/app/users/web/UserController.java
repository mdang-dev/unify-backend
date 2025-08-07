package com.unify.app.users.web;

import com.unify.app.users.domain.UserService;
import com.unify.app.users.domain.models.ShareAbleUserDto;
import com.unify.app.users.domain.models.UserDto;
import com.unify.app.users.domain.models.UserPagedResponse;
import com.unify.app.users.domain.models.UserReportCountDto;
import com.unify.app.users.domain.models.auth.CreateUserCmd;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {

  private final UserService userService;

  @GetMapping
  List<UserReportCountDto> getUsers() {
    return userService.findAllUserReportCount();
  }

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

  @GetMapping("/manage")
  ResponseEntity<UserPagedResponse> manageUsers(
      @RequestParam(required = false) String birthDay,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String firstName,
      @RequestParam(required = false) String lastName,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    try {
      // Check if any filter is applied
      boolean hasFilters =
          (birthDay != null && !birthDay.trim().isEmpty())
              || (email != null && !email.trim().isEmpty())
              || (status != null)
              || (username != null && !username.trim().isEmpty())
              || (firstName != null && !firstName.trim().isEmpty())
              || (lastName != null && !lastName.trim().isEmpty());

      // If no filters are applied, return empty result
      if (!hasFilters) {
        UserPagedResponse emptyResponse =
            new UserPagedResponse(Collections.emptyList(), 0, 0, 0, size, false, false);
        return ResponseEntity.ok(emptyResponse);
      }

      // Parse birthDay if provided
      LocalDate birthDayDate = null;
      if (birthDay != null && !birthDay.trim().isEmpty()) {
        birthDayDate = LocalDate.parse(birthDay);
      }

      Pageable pageable = PageRequest.of(page, size);
      Page<UserDto> userPage =
          userService.filterUsersWithPagination(
              birthDayDate, email, status, username, firstName, lastName, pageable);

      UserPagedResponse response =
          new UserPagedResponse(
              userPage.getContent(),
              userPage.getNumber(),
              userPage.getTotalPages(),
              userPage.getTotalElements(),
              userPage.getSize(),
              userPage.hasNext(),
              userPage.hasPrevious());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

  @PostMapping("/clear-cache/{id}")
  ResponseEntity<String> clearUserCache(@PathVariable String id) {
    userService.clearUserCache(id);
    return ResponseEntity.ok("User cache cleared successfully!");
  }
}
