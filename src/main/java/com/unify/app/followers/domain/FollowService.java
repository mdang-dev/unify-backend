package com.unify.app.followers.domain;

import com.unify.app.followers.domain.models.FollowerUserId;
import com.unify.app.followers.domain.models.FriendshipStatus;
import com.unify.app.followers.domain.models.FriendshipUserId;
import com.unify.app.notifications.domain.NotificationService;
import com.unify.app.notifications.domain.models.NotificationType;
import com.unify.app.security.SecurityService;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.UserService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowService {

  private final FollowRepository followRepository;
  private final UserService userService;
  private final SecurityService securityService;
  private final NotificationService notificationService;
  private final FriendshipRepository friendshipRepository;

  @Transactional
  private void updateFriendshipStatus(String userId1, String userId2) {
    boolean user1FollowsUser2 = isFollowing(userId1, userId2);
    boolean user2FollowsUser1 = isFollowing(userId2, userId1);

    if (user1FollowsUser2 && user2FollowsUser1) {
      // Check if friendship already exists
      FriendshipUserId friendshipId = new FriendshipUserId(userId1, userId2);
      Friendship existingFriendship = friendshipRepository.findById(friendshipId).orElse(null);

      if (existingFriendship == null) {
        // Create new friendship
        User user1 = userService.findUserById(userId1);
        User user2 = userService.findUserById(userId2);

        Friendship newFriendship =
            Friendship.builder()
                .id(friendshipId)
                .user(user1)
                .friend(user2)
                .friendshipStatus(FriendshipStatus.ACCEPTED)
                .build();

        friendshipRepository.save(newFriendship);
      } else if (existingFriendship.getFriendshipStatus() != FriendshipStatus.ACCEPTED) {
        // Update existing friendship status
        existingFriendship.setFriendshipStatus(FriendshipStatus.ACCEPTED);
        existingFriendship.setUpdateAt(LocalDateTime.now());
        friendshipRepository.save(existingFriendship);
      }
    } else {
      // If not mutual follows, remove friendship if it exists
      FriendshipUserId friendshipId = new FriendshipUserId(userId1, userId2);
      friendshipRepository.deleteById(friendshipId);
    }
  }

  @Transactional
  public String followUser(String followingId) {
    String currentUserId = securityService.getCurrentUserId();

    if (currentUserId == null) {
      throw new RuntimeException("User not authenticated");
    }

    if (currentUserId.equals(followingId)) {
      return "Can't follow yourself!";
    }

    User follower = userService.findUserById(currentUserId);

    User following = userService.findUserById(followingId);

    FollowerUserId id = new FollowerUserId(currentUserId, followingId);

    if (followRepository.existsById(id)) {
      return "You're already following this user";
    }

    try {
      Follower newFollow =
          Follower.builder().id(id).userFollower(follower).userFollowing(following).build();
      followRepository.save(newFollow);

      // Update friendship status after follow
      updateFriendshipStatus(currentUserId, followingId);

      notificationService.createAndSendNotification(
          currentUserId, followingId, NotificationType.FOLLOW);
      return "Followed successfully!";
    } catch (Exception e) {
      throw new RuntimeException("Error while following user: " + e.getMessage());
    }
  }

  @Transactional
  public String unfollowUser(String followingId) {
    String currentUserId = securityService.getCurrentUserId();

    if (currentUserId == null) {
      throw new RuntimeException("User not authenticated");
    }

    FollowerUserId id = new FollowerUserId(currentUserId, followingId);

    if (!followRepository.existsById(id)) {
      return "You're not following this user!";
    }

    try {
      followRepository.deleteById(id);

      // Update friendship status after unfollow
      updateFriendshipStatus(currentUserId, followingId);

      return "Unfollowed successfully";
    } catch (Exception e) {
      throw new RuntimeException("Error while unfollowing user: " + e.getMessage());
    }
  }

  public boolean isFollowing(String followerId, String followingId) {
    return followRepository.existsById(new FollowerUserId(followerId, followingId));
  }

  public long countFollowers(String userId) {
    return followRepository.countByUserFollowingId(userId);
  }

  public long countFollowing(String userId) {
    return followRepository.countByUserFollowerId(userId);
  }

  public boolean isFriend(String userId1, String userId2) {
    boolean user1FollowsUser2 = isFollowing(userId1, userId2);
    boolean user2FollowsUser1 = isFollowing(userId2, userId1);
    return user1FollowsUser2 && user2FollowsUser1;
  }

  public boolean shouldBeFriends(String userId1, String userId2) {
    return friendshipRepository.areFriends(userId1, userId2);
  }
}
