package com.unify.app.users.domain;

import com.unify.app.users.domain.models.*;
import com.unify.app.users.domain.models.auth.CreateUserCmd;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

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
    User user = userMapper.toUser(userDto);
    user.setPassword(encryptPassword(userDto.password()));

    // Set default values for optional fields
    if (user.getReportApprovalCount() == null) {
      user.setReportApprovalCount(0);
    }
    if (user.getGender() == null) {
      user.setGender(false); // Default to false
    }
    if (user.getStatus() == null) {
      user.setStatus(0); // Default status: normal
    }

    Role role =
        roleRepository
            .findByName("USER")
            .orElseThrow(() -> new RuntimeException("Role not found !"));
    Set<Role> roles = new HashSet<>();
    roles.add(role);
    user.setRoles(roles);

    // Save user first
    User savedUser = userRepository.save(user);

    // Create default avatar using AvatarDto
    AvatarDto avatarDto = new AvatarDto(null, avatarUrl, LocalDateTime.now());
    Avatar avatar = avatarMapper.toAvatar(avatarDto);
    avatar.setUser(savedUser);
    avatar = avatarRepository.save(avatar);

    // Set avatar to user using a mutable set to avoid Hibernate issues
    Set<Avatar> avatars = new HashSet<>();
    avatars.add(avatar);
    savedUser.setAvatars(avatars);
    savedUser = userRepository.save(savedUser);

    // streamService.createInitStream(user.getUsername(), user);
    return userMapper.toUserDTO(savedUser);
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
        return null;
      }

      // Fast negative path: avoid expensive mapping/logging on misses
      if (!userRepository.existsById(id)) {
        return null;
      }

      return userRepository.findById(id).map(userMapper::toUserDTO).orElse(null);
    } catch (Exception e) {
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
  @CacheEvict(value = "user", key = "#userDto.id")
  public UserDto updateUser(UserDto userDto) {

    User existingUser =
        userRepository
            .findById(userDto.id())
            .orElseThrow(() -> new UserNotFoundException("User not found!"));

    // Update only the fields that are provided in the DTO
    if (userDto.firstName() != null) {
      existingUser.setFirstName(userDto.firstName());
    }
    if (userDto.lastName() != null) {
      existingUser.setLastName(userDto.lastName());
    }
    if (userDto.username() != null) {
      existingUser.setUsername(userDto.username());
    }
    if (userDto.phone() != null) {
      existingUser.setPhone(userDto.phone());
    }
    if (userDto.gender() != null) {
      existingUser.setGender(userDto.gender());
    }
    if (userDto.birthDay() != null) {
      existingUser.setBirthDay(userDto.birthDay());
    }
    if (userDto.location() != null) {
      existingUser.setLocation(userDto.location());
    }
    if (userDto.education() != null) {
      existingUser.setEducation(userDto.education());
    }
    if (userDto.workAt() != null) {
      existingUser.setWorkAt(userDto.workAt());
    }
    if (userDto.biography() != null) {
      existingUser.setBiography(userDto.biography());
    }
    if (userDto.status() != null) {
      existingUser.setStatus(userDto.status());
    }

    // Handle avatar update if provided
    if (userDto.avatar() != null && userDto.avatar().url() != null) {
      // Create avatar using AvatarDto with createdAt
      AvatarDto avatarDto =
          new AvatarDto(
              userDto.avatar().id(),
              userDto.avatar().url(),
              userDto.avatar().createdAt() != null
                  ? userDto.avatar().createdAt()
                  : LocalDateTime.now());

      Avatar newAvatar = avatarMapper.toAvatar(avatarDto);
      newAvatar.setUser(existingUser);

      // Save the new avatar
      newAvatar = avatarRepository.save(newAvatar);

      // Add to existing user's avatars
      if (existingUser.getAvatars() == null) {
        existingUser.setAvatars(new HashSet<>());
      }
      existingUser.getAvatars().add(newAvatar);
    }

    User updatedUser = userRepository.save(existingUser);

    return userMapper.toUserDTO(updatedUser);
  }

  @PreAuthorize("hasRole('USER')")
  @CacheEvict(value = "user", key = "#result.id")
  public UserDto updateUser(EditProfileDto editProfileDto) {
    // Get current authenticated user
    String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    User existingUser =
        userRepository
            .findByEmail(currentUserEmail)
            .orElseThrow(() -> new UserNotFoundException("User not found!"));

    // Update only the fields that are provided in the DTO
    if (editProfileDto.firstName() != null) {
      existingUser.setFirstName(editProfileDto.firstName());
    }
    if (editProfileDto.lastName() != null) {
      existingUser.setLastName(editProfileDto.lastName());
    }
    if (editProfileDto.phone() != null) {
      existingUser.setPhone(editProfileDto.phone());
    }
    if (editProfileDto.email() != null) {
      existingUser.setEmail(editProfileDto.email());
    }
    if (editProfileDto.gender() != null) {
      existingUser.setGender(editProfileDto.gender());
    }
    if (editProfileDto.birthDay() != null) {
      existingUser.setBirthDay(editProfileDto.birthDay());
    }
    if (editProfileDto.location() != null) {
      existingUser.setLocation(editProfileDto.location());
    }
    if (editProfileDto.education() != null) {
      existingUser.setEducation(editProfileDto.education());
    }
    if (editProfileDto.workAt() != null) {
      existingUser.setWorkAt(editProfileDto.workAt());
    }
    if (editProfileDto.biography() != null) {
      existingUser.setBiography(editProfileDto.biography());
    }

    // Handle avatar update
    if (editProfileDto.avatar() != null) {
      AvatarDto avatarDto = editProfileDto.avatar();

      // If avatar has an ID, update existing avatar
      if (avatarDto.id() != null) {
        Avatar existingAvatar =
            avatarRepository
                .findById(avatarDto.id())
                .orElseThrow(() -> new RuntimeException("Avatar not found"));

        // Verify the avatar belongs to the current user
        if (!existingAvatar.getUser().getId().equals(existingUser.getId())) {
          throw new RuntimeException("Avatar does not belong to current user");
        }

        existingAvatar.setUrl(avatarDto.url());
        avatarRepository.save(existingAvatar);
      } else {
        // Create new avatar if no ID provided
        Avatar newAvatar = Avatar.builder().url(avatarDto.url()).user(existingUser).build();

        Avatar savedAvatar = avatarRepository.save(newAvatar);

        // Add to user's avatar collection
        if (existingUser.getAvatars() == null) {
          existingUser.setAvatars(new HashSet<>());
        }
        existingUser.getAvatars().add(savedAvatar);
      }
    }

    User updatedUser = userRepository.save(existingUser);
    return userMapper.toUserDTO(updatedUser);
  }

  // Clear user cache when needed
  @CacheEvict(value = "user", key = "#userId")
  public void clearUserCache(String userId) {
    // This method is used to manually clear user cache
  }

  // Get the latest avatar for a user from database
  public Avatar getLatestAvatar(String userId) {
    return avatarRepository.findLatestByUserId(userId).orElse(null);
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

    // Ensure we have the latest avatar
    Avatar latestAvatar = getLatestAvatar(user.getId());
    if (latestAvatar != null) {
      // Update the user's avatar set to only contain the latest avatar
      user.setAvatars(Set.of(latestAvatar));
    }

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

  public List<User> getFriendsNative(String currentUserId) {
    UserDto userDTO = findById(currentUserId);
    if (userDTO == null) {
      return Collections.emptyList();
    }
    return userRepository.findFriendsByUserId(userDTO.id());
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

  public Page<UserDto> filterUsersWithPagination(
      LocalDate birthDay,
      String email,
      Integer status,
      String username,
      String firstName,
      String lastName,
      Pageable pageable) {
    Page<User> userPage =
        userRepository.findUsersByFilterWithPagination(
            birthDay, email, status, username, firstName, lastName, pageable);
    return userPage.map(userMapper::toUserDTO);
  }

  public UserWithStreamDto getUserWithStreamByUserName(String username) {
      User user = this.findUserByUsername(username);
      return  userMapper.toWithStreamDto(user);
  }

  private String encryptPassword(String password) {
    return passwordEncoder.encode(password);
  }
}
