package com.unify.app.posts.domain;

import com.unify.app.posts.domain.models.Audience;
import com.unify.app.posts.domain.models.PersonalizedPostDto;
import com.unify.app.posts.domain.models.PostDto;
import com.unify.app.posts.domain.models.PostFeedResponse;
import com.unify.app.posts.domain.models.PostRowDto;
import com.unify.app.posts.domain.models.PostTableResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PostService {

  private static PostNotFoundException createPostNotFoundException() {
    return new PostNotFoundException("Post not found!");
  }

  private final PostRepository postRepository;
  private final PostMapper mapper;

  @CacheEvict(value = "personalizedFeedCache", allEntries = true)
  public PostDto createPost(PostDto postDTO) {
    Post post = mapper.toPost(postDTO);
    Post savedPost = postRepository.save(post);
    return mapper.toPostDto(savedPost);
  }

  public List<PostDto> getAll() {
    return postRepository.findAll().stream().map(mapper::toPostDto).collect(Collectors.toList());
  }

  public PostDto getById(String id) {
    Post post = postRepository.findById(id).orElseThrow(PostService::createPostNotFoundException);
    return mapper.toPostDto(post);
  }

  public Post findById(String id) {
    return postRepository.findById(id).orElseThrow(PostService::createPostNotFoundException);
  }

  public Post update(Post post) {
    var postUpdate = this.findById(post.getId());
    return postRepository.save(postUpdate);
  }

  public Optional<Post> findByOptionalPostId(String id) {
    return postRepository.findById(id);
  }

  @CacheEvict(value = "personalizedFeedCache", allEntries = true)
  public PostDto updatePost(PostDto postDto) {
    Post post =
        postRepository
            .findById(postDto.getId())
            .orElseThrow(() -> new PostNotFoundException("Post not found!"));

    // Update basic fields
    post.setCaptions(postDto.getCaptions());
    post.setAudience(postDto.getAudience());
    post.setIsCommentVisible(postDto.getIsCommentVisible());
    post.setIsLikeVisible(postDto.getIsLikeVisible());

    Post updatedPost = postRepository.save(post);
    return mapper.toPostDto(updatedPost);
  }

  public List<PostDto> getPostsTrending() {
    List<Object[]> results = postRepository.findPostsWithInteractionCounts();
    return results.stream()
        .filter(Objects::nonNull)
        .map(result -> mapper.toPostDto((Post) result[0]))
        .collect(Collectors.toList());
  }

  @CacheEvict(value = "posts", key = "#id")
  public void deletePostById(String id) {
    postRepository
        .findById(id)
        .ifPresentOrElse(
            post -> {
              post.setStatus(2);
              postRepository.save(post);
            },
            () -> {
              throw new PostNotFoundException("Post not found with id: " + id);
            });
  }

  public void archivePostById(String id) {
    postRepository
        .findById(id)
        .ifPresentOrElse(
            post -> {
              post.setStatus(post.getStatus() == 1 ? 0 : 1);
              postRepository.save(post);
            },
            () -> {
              throw new PostNotFoundException("Post not found with id: " + id);
            });
  }

  public List<PostDto> getPostsByDate(LocalDateTime start, LocalDateTime end) {
    return postRepository.getPostsByDate(start, end).stream()
        .map(mapper::toPostDto)
        .collect(Collectors.toList());
  }

  public List<PostDto> getMyPosts(String userId, Integer status, Audience audience) {
    return mapper.toPostDtoList(postRepository.findMyPosts(userId, status, audience));
  }

  public List<PostDto> getArchiveMyPosts(String userId, Integer status) {
    return mapper.toPostDtoList(postRepository.findArchiveMyPosts(userId, status));
  }

  public List<PostDto> getPostsByHashtag(String hashtag) {
    // Simplified implementation - you can add hashtag service back later
    return new ArrayList<>();
  }

  public List<PostDto> getRecommendedPosts(String userId) {
    List<Post> posts =
        postRepository.findPostsWithInteractionCounts().stream()
            .map(result -> (Post) result[0])
            .toList();
    return posts.stream().map(mapper::toPostDto).collect(Collectors.toList());
  }

  public List<PostDto> getRecommendedPostsForExplore(String userId) {
    List<Object[]> results = postRepository.findPostsWithInteractionCountsAndNotFollow(userId);
    return results.stream()
        .map(
            result -> {
              Post post = (Post) result[0];
              Long commentCount = (Long) result[1];
              PostDto postDto = mapper.toPostDto(post);
              postDto.setCommentCount(commentCount);
              return postDto;
            })
        .collect(Collectors.toList());
  }

  public List<PostDto> getPostsWithCommentCount() {
    List<Object[]> results = postRepository.findPostsWithCommentCount();
    return results.stream()
        .map(
            result -> {
              Post post = (Post) result[0];
              Long commentCount = (Long) result[1];
              PostDto postDto = mapper.toPostDto(post);
              postDto.setCommentCount(commentCount);
              return postDto;
            })
        .collect(Collectors.toList());
  }

  /**
   * Improved personalized feed algorithm that addresses: 1. Interleaving posts from different
   * sources instead of clustering by user 2. Including user's own posts (2-3 recent posts) 3.
   * Semi-randomized ordering for more dynamic feed
   */
  public PostFeedResponse getPersonalizedFeed(String userId, Pageable pageable) {
    int pageSize = pageable.getPageSize();
    int pageNumber = pageable.getPageNumber();

    // Fetch posts from different sources
    List<PersonalizedPostDto> ownPosts = postRepository.findUserOwnPosts(userId);
    List<PersonalizedPostDto> followedPosts = postRepository.findFollowedUsersPosts(userId);
    List<PersonalizedPostDto> recommendedPosts = postRepository.findRecommendedPosts(userId);

    // Create a balanced feed by interleaving posts from different sources
    List<PostDto> interleavedPosts =
        createBalancedFeed(ownPosts, followedPosts, recommendedPosts, pageSize, pageNumber, userId);

    // Determine if there are more posts available
    boolean hasNext =
        hasMorePostsAvailable(ownPosts, followedPosts, recommendedPosts, pageSize, pageNumber);

    return new PostFeedResponse(interleavedPosts, hasNext, pageNumber);
  }

  /**
   * Creates a balanced feed by interleaving posts from different sources with infinite scroll
   * support
   */
  private List<PostDto> createBalancedFeed(
      List<PersonalizedPostDto> ownPosts,
      List<PersonalizedPostDto> followedPosts,
      List<PersonalizedPostDto> recommendedPosts,
      int pageSize,
      int pageNumber,
      String userId) {

    // Create a more random seed that changes more frequently
    long baseSeed = ((long) (userId == null ? 0 : userId.hashCode())) ^ pageNumber;
    long timeSeed = System.currentTimeMillis() / 60000; // Changes every minute
    long randomSeed = baseSeed ^ timeSeed ^ (pageNumber * 31);
    Random random = new Random(randomSeed);

    // Combine all posts into a single pool for infinite scroll
    List<PersonalizedPostDto> allPostsPool = new ArrayList<>();

    // Add posts with different weights for variety
    addPostsToPool(allPostsPool, ownPosts, 1, random); // Own posts get weight 1
    addPostsToPool(allPostsPool, followedPosts, 3, random); // Followed posts get weight 3
    addPostsToPool(allPostsPool, recommendedPosts, 2, random); // Recommended posts get weight 2

    // If no posts available, return empty list
    if (allPostsPool.isEmpty()) {
      return new ArrayList<>();
    }

    // Shuffle the entire pool for maximum randomness
    Collections.shuffle(allPostsPool, random);

    // For infinite scroll, we'll cycle through posts with different starting positions
    int startIndex = (pageNumber * pageSize) % allPostsPool.size();
    List<PostDto> interleavedPosts = new ArrayList<>();

    // Fill the page with posts, cycling through the pool if needed
    for (int i = 0; i < pageSize; i++) {
      int postIndex = (startIndex + i) % allPostsPool.size();
      PersonalizedPostDto post = allPostsPool.get(postIndex);
      interleavedPosts.add(convertToPostDto(post));
    }

    // Apply additional randomization to the final order
    Collections.shuffle(interleavedPosts, new Random(randomSeed + 1));

    return interleavedPosts;
  }

  /**
   * Adds posts to the pool with specified weight (number of times to add each post) This creates
   * more variety and ensures certain posts appear more frequently
   */
  private void addPostsToPool(
      List<PersonalizedPostDto> pool, List<PersonalizedPostDto> posts, int weight, Random random) {
    if (posts.isEmpty()) return;

    // Create a shuffled copy of posts to avoid clustering
    List<PersonalizedPostDto> shuffledPosts = new ArrayList<>(posts);
    Collections.shuffle(shuffledPosts, random);

    // Add each post multiple times based on weight
    for (PersonalizedPostDto post : shuffledPosts) {
      for (int i = 0; i < weight; i++) {
        pool.add(post);
      }
    }
  }

  /** Converts PersonalizedPostDto to PostDto */
  private PostDto convertToPostDto(PersonalizedPostDto dto) {
    PostDto postDTO = mapper.toPostDto(dto.post());
    postDTO.setCommentCount(dto.commentCount());
    return postDTO;
  }

  /**
   * Determines if there are more posts available for pagination - always true for infinite scroll
   */
  private boolean hasMorePostsAvailable(
      List<PersonalizedPostDto> ownPosts,
      List<PersonalizedPostDto> followedPosts,
      List<PersonalizedPostDto> recommendedPosts,
      int pageSize,
      int pageNumber) {

    // For infinite scroll, we always return true as long as there are any posts available
    // The algorithm will cycle through posts infinitely
    return !ownPosts.isEmpty() || !followedPosts.isEmpty() || !recommendedPosts.isEmpty();
  }

  public Page<PostDto> getReelsPosts(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Object[]> postPage = postRepository.findReelsPostsWithCommentCount(pageable);
    return postPage.map(
        result -> {
          Post post = (Post) result[0];
          Long commentCount = (Long) result[1];
          PostDto postDto = mapper.toPostDto(post);
          postDto.setCommentCount(commentCount);
          return postDto;
        });
  }

  // Removed problematic methods to focus on core newsfeed functionality

  public PostTableResponse getPostsForTable(
      String captions,
      Integer status,
      Audience audience,
      Boolean isCommentVisible,
      Boolean isLikeVisible,
      String hashtag,
      Long commentCount,
      String commentCountOperator,
      int page,
      int size) {

    Pageable pageable = PageRequest.of(page, size);

    // Convert Audience enum to string for native query
    String audienceString = audience != null ? audience.name() : null;

    Page<Object[]> result =
        postRepository.findPostsForTable(
            captions,
            status,
            audienceString,
            isCommentVisible,
            isLikeVisible,
            hashtag,
            commentCount,
            commentCountOperator,
            pageable);

    List<PostRowDto> rows = new ArrayList<>();
    int currentIndex = page * size + 1; // Start index for current page

    for (Object[] row : result.getContent()) {
      // Map database columns to PostRowDto
      String postId = (String) row[0];
      String postCaptions = (String) row[1];
      Integer postStatus = (Integer) row[2];
      String postAudience = (String) row[3];
      LocalDateTime postPostedAt = convertToLocalDateTime(row[4]);
      // row[5] - is_comment_visible (not needed for display)
      // row[6] - is_like_visible (not needed for display)
      // row[7] - updated_at (not needed for display)
      // row[8] - user_id (not needed for display)
      String firstName = (String) row[9];
      String lastName = (String) row[10];
      String username = (String) row[11];
      Long postCommentCount = ((Number) row[12]).longValue();

      // Construct user display - prefer full name, fallback to @username
      String userDisplay;
      if (firstName != null
          && lastName != null
          && !firstName.trim().isEmpty()
          && !lastName.trim().isEmpty()) {
        userDisplay = firstName.trim() + " " + lastName.trim();
      } else if (firstName != null && !firstName.trim().isEmpty()) {
        userDisplay = firstName.trim();
      } else if (lastName != null && !lastName.trim().isEmpty()) {
        userDisplay = lastName.trim();
      } else {
        userDisplay = "@" + (username != null ? username : "unknown");
      }

      PostRowDto postRow =
          new PostRowDto(
              currentIndex++,
              userDisplay,
              postCaptions,
              postStatus,
              postAudience,
              postPostedAt,
              postCommentCount,
              new PostRowDto.PostActionDto(postId));

      rows.add(postRow);
    }

    return new PostTableResponse(
        rows,
        page + 1, // Convert 0-based to 1-based page number
        size,
        result.getTotalElements());
  }

  /**
   * Helper method to convert various date/time types to LocalDateTime Handles conversion from
   * Timestamp (native SQL) to LocalDateTime
   */
  private LocalDateTime convertToLocalDateTime(Object dateTimeObject) {
    if (dateTimeObject == null) {
      return null;
    }

    if (dateTimeObject instanceof LocalDateTime) {
      return (LocalDateTime) dateTimeObject;
    }

    if (dateTimeObject instanceof java.sql.Timestamp) {
      return ((java.sql.Timestamp) dateTimeObject).toLocalDateTime();
    }

    if (dateTimeObject instanceof java.util.Date) {
      return LocalDateTime.ofInstant(
          ((java.util.Date) dateTimeObject).toInstant(), ZoneId.systemDefault());
    }

    // If we get here, we have an unexpected type
    throw new IllegalArgumentException(
        "Cannot convert "
            + dateTimeObject.getClass().getName()
            + " to LocalDateTime: "
            + dateTimeObject);
  }
}
