package com.unify.app.messages.domain;

import com.unify.app.followers.domain.FollowService;
import com.unify.app.messages.domain.models.SharePostRequestDto;
import com.unify.app.messages.domain.models.SharePostResponseDto;
import com.unify.app.notifications.domain.NotificationService;
import com.unify.app.notifications.domain.models.NotificationType;
import com.unify.app.posts.domain.Post;
import com.unify.app.posts.domain.PostService;
import com.unify.app.security.SecurityService;
import com.unify.app.users.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService {
  private final PostService postService;
  private final FollowService followService;
  private final SecurityService securityService;
  private final NotificationService notificationService; // ✅ NEW: Add notification service

  public SharePostResponseDto sharePost(SharePostRequestDto request) {
    try {
      Post post = postService.findById(request.postId());
      User postOwner = post.getUser();
      String currentUserId = securityService.getCurrentUserId();

      // Check friendship
      if (!followService.shouldBeFriends(currentUserId, postOwner.getId())) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "You are not friends with the post owner");
      }

      // ✅ NEW: Send real-time notification to post owner about the share
      if (!currentUserId.equals(postOwner.getId())) {
        try {
          String message = "Someone shared your post";
          String link = "/posts/" + post.getId();
          String data =
              String.format("{\"postId\":\"%s\",\"sharedBy\":\"%s\"}", post.getId(), currentUserId);

          notificationService.createAndSendNotification(
              currentUserId, postOwner.getId(), NotificationType.SHARE, message, link, data);

          log.info(
              "Share notification sent to post owner {} for post {}",
              postOwner.getId(),
              post.getId());
        } catch (Exception e) {
          log.error("Failed to send share notification: {}", e.getMessage(), e);
          // Don't fail the share operation if notification fails
        }
      }

      // Generate share link (customize as needed)
      String shareLink = "/posts/" + post.getId();
      return new SharePostResponseDto(shareLink);

    } catch (Exception e) {
      log.error("Failed to share post: {}", e.getMessage(), e);
      throw e;
    }
  }
}
