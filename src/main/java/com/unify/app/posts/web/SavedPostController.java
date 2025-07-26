package com.unify.app.posts.web;

import com.unify.app.posts.domain.models.PostDto;
import com.unify.app.posts.saved.SavedPostDto;
import com.unify.app.posts.saved.SavedPostService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/savedPosts")
@RequiredArgsConstructor
class SavedPostController {

  private final SavedPostService savedPostService;

  @PostMapping("/add/{userId}/{postId}")
  public ResponseEntity<String> toggleSavedPost(
      @PathVariable String userId, @PathVariable String postId) {
    savedPostService.toggleSavePost(userId, postId);
    return ResponseEntity.ok("Toggled saved post successfully.");
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> unsavePost(@PathVariable String id) {
    savedPostService.unsavePostById(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{username}")
  public ResponseEntity<List<SavedPostDto>> getSavedPostsByUser(@PathVariable String username) {
    List<SavedPostDto> savedPosts = savedPostService.getSavedPostsByUsername(username);
    return ResponseEntity.ok(savedPosts);
  }

  @GetMapping("/{userId}/{postId}/exists")
  public ResponseEntity<Boolean> isPostSaved(
      @PathVariable String userId, @PathVariable String postId) {
    return ResponseEntity.ok(savedPostService.isPostSaved(userId, postId));
  }

  @GetMapping("/post/{postId}")
  public ResponseEntity<List<PostDto>> getSavedPostDetails(@PathVariable String postId) {
    return ResponseEntity.ok(savedPostService.getSavedPostDetails(postId));
  }
}
