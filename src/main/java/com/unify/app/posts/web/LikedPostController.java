package com.unify.app.posts.web;

import com.unify.app.posts.domain.models.PostDto;
import com.unify.app.posts.liked.LikedPostService;
import com.unify.app.posts.liked.models.LikedPostRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/liked-posts")
@RequiredArgsConstructor
class LikedPostController {

  private final LikedPostService likedPostService;

  @GetMapping("/{id}")
  public List<PostDto> getListLikedPosts(@PathVariable String id) {
    return likedPostService.getListLikedPosts(id);
  }

  @GetMapping("/countLiked/{postId}")
  public ResponseEntity<Integer> countLiked(@PathVariable String postId) {
    int likeCount = likedPostService.countLikePost(postId);
    return ResponseEntity.ok(likeCount);
  }

  @GetMapping("/is-liked/{userId}/{postId}")
  public ResponseEntity<Boolean> isLiked(@PathVariable String userId, @PathVariable String postId) {
    boolean isLiked = likedPostService.checkLiked(userId, postId);
    return ResponseEntity.ok(isLiked);
  }

  @PostMapping
  public ResponseEntity<?> save(@Valid @RequestBody LikedPostRequest request) {
    likedPostService.createLikedPost(request);
    return ResponseEntity.ok("You liked this post !");
  }

  @DeleteMapping("/{userId}/{postId}")
  public ResponseEntity<?> remove(@PathVariable String userId, @PathVariable String postId) {
    likedPostService.deleteLikedPost(new LikedPostRequest(userId, postId));
    return ResponseEntity.ok("You canceled liking this post!");
  }
}
