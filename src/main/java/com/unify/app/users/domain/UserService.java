package com.unify.app.users.domain;

import com.unify.app.users.domain.models.ShareAbleUserDto;
import com.unify.app.users.domain.models.UserDto;
import com.unify.app.users.domain.models.UserReportCountDto;
import com.unify.app.users.domain.models.auth.CreateUserCmd;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final AvatarMapper avatarMapper;
  private final AvatarRepository avatarRepository;

  @Value("${var.avatar}")
  private String avatarUrl;

  @PreAuthorize("hasRole('ADMIN')")
  public List<UserDto> findAllUserByRole() {
    return userRepository.findAllUserByRole().stream()
        .map(userMapper::toUserDTO)
        .collect(Collectors.toList());
  }

  @PreAuthorize("hasRole('ADMIN')")
  public List<UserReportCountDto> findAllUserReportCount() {
    List<UserReportCountDto> usersWithReports = userRepository.findAllUserAndCountReportByRole();
    usersWithReports.forEach(
        dto -> {
          String id = dto.id();
          String username = dto.username();
          Long reportCount = dto.reportApprovalCount();
        });
    return usersWithReports;
  }

  public UserDto createUser(CreateUserCmd userDto) {
    try {
      User user = userMapper.toUser(userDto);
      user.setPassword(encryptPassword(userDto.password()));
      user.setStatus(0); // Set default status to normal
      if (user.getReportApprovalCount() == null) {
        user.setReportApprovalCount(0);
      }
      Role role =
          roleRepository
              .findByName("USER")
              .orElseThrow(() -> new RuntimeException("Role not found !"));
      user.setRoles(Collections.singleton(role));

      // Save the user first
      User savedUser = userRepository.save(user);

      // Create and save avatar with error handling
      try {
        Avatar avatar =
            Avatar.builder()
                .url(avatarUrl != null ? avatarUrl : "https://via.placeholder.com/150")
                .user(savedUser)
                .build();
        avatarRepository.save(avatar);

        // Update user with avatar
        savedUser.setAvatars(Set.of(avatar));
        savedUser = userRepository.save(savedUser);
      } catch (Exception avatarError) {
        log.warn(
            "Avatar creation failed for user {}, but user was saved: {}",
            savedUser.getUsername(),
            avatarError.getMessage());
        // User is already saved, continue without avatar
      }

      return userMapper.toUserDTO(savedUser);
    } catch (Exception e) {
      log.error("Error creating user: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
    }
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public void updatePassword(String email, String password) {
    userRepository.updatePasswordByEmail(email, password);
  }

  public boolean existsByUsername(String email) {
    return userRepository.existsByUsername(email);
  }

  public List<User> findAllById(List<String> ids) {
    return userRepository.findAllById(ids);
  }

  // @PreAuthorize("hasRole('ADMIN')")

  @Cacheable(value = "user", key = "#id")
  public UserDto findById(String id) {
    return userMapper.toUserDTO(
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found !")));
  }

  // ✅ FIX: Safe method that returns null instead of throwing exception
  public UserDto findByIdSafe(String id) {
    try {
      if (id == null || id.trim().isEmpty()) {
        System.err.println("Error: User ID is null or empty");
        return null;
      }

      var user = userRepository.findById(id).orElse(null);
      if (user == null) {
        System.err.println("User not found in database for ID: " + id);
        return null;
      }

      return userMapper.toUserDTO(user);
    } catch (Exception e) {
      System.err.println("Error finding user by ID: " + id + " - " + e.getMessage());
      System.err.println("Error type: " + e.getClass().getSimpleName());
      e.printStackTrace();
      return null;
    }
  }

  public User update(User user) {
    var updateUser = this.findUserById(user.getId());
    return userRepository.save(updateUser);
  }

  public User findUserById(String id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found !"));
  }

  // @PreAuthorize("#userDto.email == authentication.name")
  // public UserDTO updateUser(UserDTO userDto) {
  // Role role = roleRepository.findByName("USER").orElseThrow(() -> new
  // RuntimeException("Role not found !"));
  // userDto.setPassword(userRepository.findById(userDto.getId())
  // .orElseThrow(() -> new UserNotFoundException("User not found
  // !")).getPassword());
  //
  // userDto.setRoles(Collections.singleton(role));
  // User user = userRepository.save(userMapper.toUser(userDto));
  // return userMapper.toUserDTO(user);
  // }

  @PreAuthorize("#userDto.email == authentication.name")
  public UserDto updateUser(UserDto userDto) {

    User existingUser =
        userRepository
            .findById(userDto.id())
            .orElseThrow(() -> new UserNotFoundException("User not found!"));

    User updatedUser = userMapper.toUser(userDto);
    updatedUser.setReportApprovalCount(existingUser.getReportApprovalCount());
    updatedUser.setPassword(existingUser.getPassword());
    if (updatedUser.getAvatars() != null) {
      Avatar newAvatar = avatarMapper.toAvatar(userDto.avatar());
      newAvatar.setUser(updatedUser);

      if (updatedUser.getAvatars() == null) {
        updatedUser.setAvatars(Set.of());
      }
      updatedUser.setAvatars(Set.of(newAvatar));
    } else {
      updatedUser.setAvatars(existingUser.getAvatars());
    }
    updatedUser = userRepository.save(updatedUser);

    return userMapper.toUserDTO(updatedUser);
  }

  @PreAuthorize("hasRole('ADMIN')")
  public void removeUser(String id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found !"));
    userRepository.delete(user);
  }

  @PreAuthorize("hasRole('ADMIN')")
  public void temporarilyDisableUser(String id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found !"));
    user.setStatus(1);
    userRepository.save(user);
  }

  @PreAuthorize("hasRole('ADMIN')")
  public void permanentlyDisableUser(String id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found !"));
    user.setStatus(2);
    userRepository.save(user);
  }

  @PreAuthorize("hasRole('ADMIN')")
  public void unlockUser(String id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found !"));
    user.setStatus(0);
    userRepository.save(user);
  }

  public UserDto getMyInfo() {
    var context = SecurityContextHolder.getContext();
    String name = context.getAuthentication().getName();
    User user =
        userRepository
            .findByEmail(name)
            .orElseThrow(() -> new UserNotFoundException("User not found !"));
    return userMapper.toUserDTO(user);
  }

  public UserDto findByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .map(userMapper::toUserDTO)
        .orElseThrow(() -> new UserNotFoundException("Username not found: " + username));
  }

  public User findUserByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new UserNotFoundException("Username not found: " + username));
  }

  public User findByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException("User not found !"));
  }

  public List<UserDto> getSuggestedUsers(String currentUserId) {
    UserDto userDto = findById(currentUserId);
    if (userDto == null) {
      return Collections.emptyList();
    }

    int limit = 10;

    // Lấy người có bạn chung trước
    List<User> mutuals =
        userRepository.findSuggestedFriendsWithMutualFriends(
            userDto.id(), PageRequest.of(0, limit));

    List<UserDto> suggestedUsers =
        mutuals.stream().map(userMapper::toUserDTO).collect(Collectors.toList());

    // Nếu chưa đủ thì lấy thêm người lạ
    if (suggestedUsers.size() < limit) {
      int remaining = limit - suggestedUsers.size();
      List<String> mutualIds = mutuals.stream().map(User::getId).collect(Collectors.toList());

      List<User> strangers =
          userRepository.findSuggestedStrangersExcluding(
              userDto.id(), mutualIds, PageRequest.of(0, remaining));

      List<UserDto> strangerDTOs =
          strangers.stream().map(userMapper::toUserDTO).collect(Collectors.toList());

      suggestedUsers.addAll(strangerDTOs);
    }

    return suggestedUsers;
  }

  public List<UserDto> findUsersFollowingMe(String currentUserId) {
    UserDto userDto = findById(currentUserId);
    if (userDto == null) {
      return Collections.emptyList();
    }
    return userRepository.findUsersFollowingMe(userDto.id()).stream()
        .map(userMapper::toUserDTO)
        .collect(Collectors.toList());
  }

  public List<UserDto> findUsersFollowedBy(String currentUserId) {
    UserDto userDto = findById(currentUserId);
    if (userDto == null) {
      return Collections.emptyList();
    }
    List<User> mutualUsers = userRepository.findUsersFollowedBy(userDto.id());
    return mutualUsers.stream().map(userMapper::toUserDTO).collect(Collectors.toList());
  }

  public List<User> findUsersNativeFollowedBy(String currentUserId) {
    UserDto userDTO = findById(currentUserId);
    if (userDTO == null) {
      return Collections.emptyList();
    }
    return userRepository.findUsersFollowedBy(userDTO.id());
  }

  public List<UserDto> searchUsers(String query) {
    List<User> users =
        userRepository
            .findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                query, query, query);
    return users.stream().map(userMapper::toUserDTO).collect(Collectors.toList());
  }

  public void changePassword(String currentPassword, String newPassword) {
    var context = SecurityContextHolder.getContext();
    String email = context.getAuthentication().getName();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found!"));
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  public List<UserDto> getFriends(String currentUserId) {
    UserDto userDTO = findById(currentUserId);
    if (userDTO == null) {
      return Collections.emptyList();
    }
    return userRepository.findFriendsByUserId(userDTO.id()).stream()
        .map(userMapper::toUserDTO)
        .collect(Collectors.toList());
  }

  public List<ShareAbleUserDto> getMutualFollowers(String myId) {
    List<User> mutualUsers = userRepository.findMutualFollowingUsers(myId);
    return mutualUsers.stream()
        .map(
            user -> {
              Avatar latestAvatar = user.latestAvatar();
              return new ShareAbleUserDto(
                  user.getId(),
                  user.getUsername(),
                  user.getFirstName() + " " + user.getLastName(),
                  latestAvatar != null
                      ? String.valueOf(avatarMapper.toAvatarDTO(latestAvatar))
                      : null);
            })
        .toList();
  }

  private String encryptPassword(String password) {
    return passwordEncoder.encode(password);
  }
}
