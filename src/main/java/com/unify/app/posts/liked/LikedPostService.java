package com.unify.app.posts.liked;

import com.unify.app.notifications.domain.NotificationService;
import com.unify.app.notifications.domain.models.NotificationType;
import com.unify.app.posts.domain.PostMapper;
import com.unify.app.posts.domain.PostService;
import com.unify.app.posts.domain.models.PostDto;
import com.unify.app.posts.liked.models.LikedPostRequest;
import com.unify.app.users.domain.UserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LikedPostService {

  private final PostService postService;
  private final UserService userService;
  private final LikedPostRepository likedPostRepository;
  private final PostMapper postMapper;
  private final NotificationService notificationService;

  public List<PostDto> getListLikedPosts(String userId) {
    List<LikedPost> likedPosts = likedPostRepository.findAllByUserId(userId);
    return likedPosts.stream()
        .map(LikedPost::getPost)
        .map(postMapper::toPostDto)
        .collect(Collectors.toList());
  }

  public boolean checkLiked(String userId, String postId) {
    return likedPostRepository.existsByUserIdAndPostId(userId, postId);
  }

  public void createLikedPost(LikedPostRequest request) {
    try {
      // ✅ SECURITY: Validate request parameters
      if (request.userId() == null || request.postId() == null) {
        log.warn("Invalid like request: userId={}, postId={}", request.userId(), request.postId());
        throw new IllegalArgumentException("Invalid request parameters");
      }

      // ✅ PERFORMANCE: Check if already liked to avoid duplicate
      if (likedPostRepository.existsByUserIdAndPostId(request.userId(), request.postId())) {
        return;
      }

      LikedPost likedPost =
          LikedPost.builder()
              .post(postService.findById(request.postId()))
              .user(userService.findUserById(request.userId()))
              .build();

      likedPostRepository.save(likedPost);

      // ✅ NOTIFICATION: Send notification to post owner (only if not already liked)
      String postOwnerId = likedPost.getPost().getUser().getId();
      if (!request.userId().equals(postOwnerId)) {
        try {
          notificationService.createAndSendNotification(
              request.userId(),
              postOwnerId,
              NotificationType.LIKE,
              null, // Use default message
              "/posts/" + request.postId() // Link to the post
              );

        } catch (Exception e) {
          log.error("Failed to send like notification: {}", e.getMessage(), e);
          // Don't fail the like operation if notification fails
        }
      }
    } catch (Exception e) {
      log.error("Failed to create liked post: {}", e.getMessage(), e);
      throw e;
    }
  }

  public void deleteLikedPost(LikedPostRequest request) {
    try {
      // ✅ SECURITY: Validate request parameters
      if (request.userId() == null || request.postId() == null) {
        log.warn(
            "Invalid unlike request: userId={}, postId={}", request.userId(), request.postId());
        throw new IllegalArgumentException("Invalid request parameters");
      }

      LikedPost likedPost =
          likedPostRepository.findByUserIdAndPostId(request.userId(), request.postId());
      if (likedPost == null) {
        throw new IllegalStateException("No liked post found for this user and post");
      }

      likedPostRepository.deleteByUserIdAndPostId(request.userId(), request.postId());


    } catch (Exception e) {
      log.error("Failed to delete liked post: {}", e.getMessage(), e);
      throw e;
    }
  }

  public int countLikePost(String postId) {
    return likedPostRepository.countByPostId(postId);
  }
}
