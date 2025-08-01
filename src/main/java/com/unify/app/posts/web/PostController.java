package com.unify.app.posts.web;

import com.unify.app.posts.domain.PostService;
import com.unify.app.posts.domain.models.Audience;
import com.unify.app.posts.domain.models.PostDto;
import com.unify.app.posts.domain.models.PostFeedResponse;
import com.unify.app.security.SecurityService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
class PostController {

  private final PostService postService;
  private final SecurityService securityService;

  @GetMapping
  public ResponseEntity<List<PostDto>> getAllPosts() {
    return ResponseEntity.ok(postService.getPostsWithCommentCount()); // Sửa để trả commentCount
  }

  @GetMapping("/personalized")
  ResponseEntity<?> getPersonalizedFeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "7") int size) {

    String userId = securityService.getCurrentUserId();

    Pageable pageable = PageRequest.of(page, size, Sort.by("postedAt").descending());

    PostFeedResponse response = postService.getPersonalizedFeed(userId, pageable);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/reels")
  public ResponseEntity<PostFeedResponse> getReelsPosts(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "7") int size) {
    Page<PostDto> postPage = postService.getReelsPosts(page, size);

    PostFeedResponse response =
        new PostFeedResponse(postPage.getContent(), postPage.hasNext(), postPage.getNumber());

    return ResponseEntity.ok(response);
  }

  @PostMapping
  public PostDto createPost(@RequestBody PostDto postDTO) {
    return postService.createPost(postDTO);
  }

  @GetMapping("/post_detail/{id}")
  public PostDto getPost(@PathVariable("id") String id) {
    return postService.getById(id);
  }

  @PutMapping
  public PostDto updatePost(@RequestBody PostDto postDTO) {
    return postService.updatePost(postDTO);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deletePost(@PathVariable("id") String id) {
    postService.deletePostById(id);
    return ResponseEntity.ok("Post deleted successfully!");
  }

  @PutMapping("/{id}/archive")
  public ResponseEntity<String> archivePost(@PathVariable("id") String id) {
    postService.archivePostById(id);
    return ResponseEntity.ok("Successfully moved to archive!");
  }

  @GetMapping("/admin/list")
  public List<PostDto> getPostList() {
    return postService.getAll();
  }

  @GetMapping("/filter/{start}/{end}")
  public List<PostDto> getPostsByDate(
      @PathVariable("start") String start, @PathVariable("end") String end) {
    LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    LocalDateTime endDateTime = endDate.atStartOfDay();
    return postService.getPostsByDate(startDateTime, endDateTime);
  }

  // @GetMapping("/username/{username}")
  // public List<PostDTO> getMyPosts(@PathVariable("username") String username) {
  // return postService.getMyPosts(username);
  // }

  @GetMapping("/my")
  public ResponseEntity<List<PostDto>> getMyPosts(
      @RequestParam String userId, @RequestParam Integer status, @RequestParam Audience audience) {
    List<PostDto> posts = postService.getMyPosts(userId, status, audience);
    return ResponseEntity.ok(posts);
  }

  @GetMapping("/myArchive")
  public ResponseEntity<List<PostDto>> getArchiveMyPosts(
      @RequestParam String userId, @RequestParam Integer status) {
    List<PostDto> posts = postService.getArchiveMyPosts(userId, status);
    return ResponseEntity.ok(posts);
  }

  @GetMapping("/hashtag/{content}")
  public ResponseEntity<List<PostDto>> getPostsByHashtag(@PathVariable("content") String content) {
    return ResponseEntity.ok(postService.getPostsByHashtag("#" + content));
  }

  @GetMapping("/explorer")
  public ResponseEntity<List<PostDto>> getRecommendedPostsForExplore() {
    String userId = securityService.getCurrentUserId();
    List<PostDto> posts = postService.getRecommendedPostsForExplore(userId);
    return ResponseEntity.ok(posts);
  }
}
