package com.unify.app.comments.domain;

import com.unify.app.notifications.domain.NotificationService;
import com.unify.app.notifications.domain.models.NotificationType;
import com.unify.app.posts.domain.Post;
import com.unify.app.posts.domain.PostService;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserService userService;
  private final PostService postService;
  private final NotificationService notificationService; // ✅ NEW: Notification integration

  /**
   * Save a comment to a post.
   *
   * @param userId ID of the user writing the comment
   * @param postId ID of the post to comment on
   * @param content Comment content
   * @param parentId ID of the parent comment (for replies), can be null
   * @return Saved PostComment entity
   */
  public Comment saveComment(String userId, String postId, String content, String parentId) {
    try {
      if (content == null || content.trim().isEmpty()) {
        throw new IllegalArgumentException("Comment content must not be empty");
      }

      User user = userService.findUserById(userId);
      Post post = postService.findById(postId);

      if (Boolean.TRUE.equals(post.getIsCommentVisible())) {
        throw new IllegalArgumentException("Comments are disabled for this post");
      }

      if (post.getStatus() == 0) {
        throw new IllegalArgumentException("This post is not available for commenting");
      }

      Comment parent = null;
      if (parentId != null && !parentId.isEmpty()) {
        parent =
            commentRepository
                .findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));

        if (parent.getStatus() == 2) {
          throw new IllegalArgumentException("Cannot reply to a hidden comment");
        }
      }

      Comment newComment =
          Comment.builder()
              .user(user)
              .post(post)
              .content(content)
              .parent(parent)
              .status(0) // Default to visible
              .build();

      Comment savedComment = commentRepository.save(newComment);

      // ✅ NEW: Send notification for comment
      sendCommentNotification(userId, post, parent);

      log.info("Saved comment with ID: {} for post: {}", savedComment.getId(), postId);
      return savedComment;
    } catch (Exception e) {
      log.error("Failed to save comment: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to save comment", e);
    }
  }

  // ✅ NEW: Send comment notification
  private void sendCommentNotification(String commenterId, Post post, Comment parent) {
    try {
      String postAuthorId = post.getUser().getId();

      // Don't send notification if user comments on their own post
      if (commenterId.equals(postAuthorId)) {
        log.debug(
            "User {} commented on their own post {}, skipping notification",
            commenterId,
            post.getId());
        return;
      }

      // For replies, notify the parent comment author
      String notificationReceiverId = parent != null ? parent.getUser().getId() : postAuthorId;

      // Don't send notification if replying to own comment
      if (commenterId.equals(notificationReceiverId)) {
        log.debug("User {} replied to their own comment, skipping notification", commenterId);
        return;
      }

      String message = generateCommentMessage(commenterId, parent != null);
      String link = "/posts/" + post.getId();

      log.debug(
          "Sending comment notification: user {} commented on post {} by user {}",
          commenterId,
          post.getId(),
          notificationReceiverId);

      notificationService.createAndSendNotification(
          commenterId, notificationReceiverId, NotificationType.COMMENT, message, link);
    } catch (Exception e) {
      log.error("Failed to send comment notification: {}", e.getMessage(), e);
      // Don't throw exception to avoid breaking comment creation
    }
  }

  // ✅ NEW: Generate comment notification message
  private String generateCommentMessage(String commenterId, boolean isReply) {
    try {
      var commenter = userService.findUserById(commenterId);
      String commenterName = commenter.getFirstName() + " " + commenter.getLastName();
      return commenterName + (isReply ? " replied to your comment." : " commented on your post.");
    } catch (Exception e) {
      log.warn("Failed to generate comment message for user {}: {}", commenterId, e.getMessage());
      return "Someone " + (isReply ? "replied to your comment." : "commented on your post.");
    }
  }

  /**
   * Get top-level comments for a post (status = 0 only). Replies will be nested under each
   * top-level comment.
   *
   * @param postId ID of the post
   * @return List of CommentDTOs
   */
  public List<CommentDto> getCommentsByPostId(String postId) {
    try {
      Post post = postService.findById(postId);

      // If comments are hidden, return empty list
      if (Boolean.TRUE.equals(post.getIsCommentVisible())) {
        return List.of();
      }

      // Fetch all comments with status 0 for the post
      List<Comment> allComments = commentRepository.findAllCommentsByPostIdAndStatus(postId, 0);

      // Extract root (top-level) comments
      List<Comment> rootComments =
          allComments.stream().filter(c -> c.getParent() == null).collect(Collectors.toList());

      // Map each comment ID to its Comment object
      Map<String, Comment> commentMap =
          allComments.stream().collect(Collectors.toMap(Comment::getId, c -> c));

      // Set replies for each comment
      for (Comment comment : allComments) {
        if (comment.getParent() != null) {
          Comment parent = commentMap.get(comment.getParent().getId());
          if (parent != null) {
            parent.getReplies().add(comment); // assuming `replies` is initialized
          }
        }
      }

      // Convert only root comments to DTOs
      return rootComments.stream().map(this::convertToDto).collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Failed to get comments for post {}: {}", postId, e.getMessage(), e);
      return List.of();
    }
  }

  public Optional<Comment> findOptionalById(String id) {
    return commentRepository.findById(id);
  }

  public Comment findById(String id) {
    return commentRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + id));
  }

  public Comment update(Comment comment) {
    try {
      var updateComment = this.findById(comment.getId());
      return commentRepository.save(updateComment);
    } catch (Exception e) {
      log.error("Failed to update comment {}: {}", comment.getId(), e.getMessage(), e);
      throw new RuntimeException("Failed to update comment", e);
    }
  }

  public List<Comment> findByParentId(String id) {
    return commentRepository.findByParentId(id);
  }

  /**
   * Convert Comment entity to CommentDTO.
   *
   * @param comment Comment entity
   * @return CommentDTO with nested replies
   */
  private CommentDto convertToDto(Comment comment) {
    return new CommentDto(comment);
  }

  /**
   * Delete a comment and its associated reports by ID.
   *
   * @param commentId ID of the comment to delete
   */
  public void deleteCommentById(String commentId) {
    try {
      Comment comment =
          commentRepository
              .findById(commentId)
              .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

      if (comment.getStatus() == 2) {
        log.warn("Deleting hidden comment with ID: {}", commentId);
      }

      commentRepository.delete(comment);
      log.info("Deleted comment with ID: {}", commentId);
    } catch (Exception e) {
      log.error("Failed to delete comment {}: {}", commentId, e.getMessage(), e);
      throw new RuntimeException("Failed to delete comment", e);
    }
  }
}
