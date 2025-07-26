package com.unify.app.comments.web;

import com.unify.app.comments.domain.Comment;
import com.unify.app.comments.domain.CommentDto;
import com.unify.app.comments.domain.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  /** Add a new comment to a post */
  @PostMapping
  public ResponseEntity<?> addComment(@RequestBody CommentDto request) {
    Comment savedComment =
        commentService.saveComment(
            request.userId(), request.postId(), request.content(), request.parentId());
    return ResponseEntity.ok(new CommentDto(savedComment));
  }

  /** Get all comments for a post */
  @GetMapping("/{postId}")
  public ResponseEntity<List<CommentDto>> getCommentsByPostId(@PathVariable String postId) {
    List<CommentDto> comments = commentService.getCommentsByPostId(postId);
    return ResponseEntity.ok(comments);
  }

  /** Delete a comment by ID */
  @DeleteMapping("/{commentId}")
  public ResponseEntity<?> deleteComment(@PathVariable String commentId) {
    commentService.deleteCommentById(commentId);
    return ResponseEntity.ok("Comment deleted successfully");
  }
}
