package com.unify.app.comments.domain;

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

  /**
   * Save a comment to a post.
   *
   * @param userId   ID of the user writing the comment
   * @param postId   ID of the post to comment on
   * @param content  Comment content
   * @param parentId ID of the parent comment (for replies), can be null
   * @return Saved PostComment entity
   */
  public Comment saveComment(String userId, String postId, String content, String parentId) {
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
      parent = commentRepository
          .findById(parentId)
          .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));

      if (parent.getStatus() == 2) {
        throw new IllegalArgumentException("Cannot reply to a hidden comment");
      }
    }

    Comment newComment = Comment.builder()
        .user(user)
        .post(post)
        .content(content)
        .parent(parent)
        .status(0) // Default to visible
        .build();

    Comment savedComment = commentRepository.save(newComment);
    log.info("Saved comment with ID: {}", savedComment.getId());
    return savedComment;
  }

  /**
   * Get top-level comments for a post (status = 0 only). Replies will be nested
   * under each
   * top-level comment.
   *
   * @param postId ID of the post
   * @return List of CommentDTOs
   */
  public List<CommentDto> getCommentsByPostId(String postId) {
    Post post = postService.findById(postId);

    // If comments are hidden, return empty list
    if (Boolean.TRUE.equals(post.getIsCommentVisible())) {
      return List.of();
    }

    // Fetch all comments with status 0 for the post
    List<Comment> allComments = commentRepository.findAllCommentsByPostIdAndStatus(postId, 0);

    // Extract root (top-level) comments
    List<Comment> rootComments = allComments.stream().filter(c -> c.getParent() == null).collect(Collectors.toList());

    // Map each comment ID to its Comment object
    Map<String, Comment> commentMap = allComments.stream().collect(Collectors.toMap(Comment::getId, c -> c));

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
  }

  public Optional<Comment> findOptionalById(String id) {
    return commentRepository.findById(id);
  }

  public Comment findById(String id) {
    return commentRepository
        .findById(id)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + id));
  }

  public Comment update(Comment comment) {
    var updateComment = this.findById(comment.getId());
    return commentRepository.save(updateComment);
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
    Comment comment = commentRepository
        .findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

    if (comment.getStatus() == 2) {
      log.warn("Deleting hidden comment with ID: {}", commentId);
    }
  }
}
