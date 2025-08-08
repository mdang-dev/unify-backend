package com.unify.app.posts.domain;

import com.unify.app.hashtags.domain.HashtagService;
import com.unify.app.hashtags.domain.models.HashtagDetailDto;
import com.unify.app.hashtags.domain.models.HashtagDto;
import com.unify.app.posts.domain.models.Audience;
import com.unify.app.posts.domain.models.MediaDto;
import com.unify.app.posts.domain.models.PersonalizedPostDto;
import com.unify.app.posts.domain.models.PostDto;
import com.unify.app.posts.domain.models.PostFeedResponse;
import com.unify.app.posts.domain.models.PostFilterDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
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
  private final MediaMapper mediaMapper;
  private final HashtagService hashtagService;
  private final MediaRepository mediaRepository;

  @CacheEvict(value = "personalizedFeedCache", allEntries = true)
  public PostDto createPost(PostDto postDTO) {
    Post post = mapper.toPost(postDTO);
    Post savedPost = postRepository.save(post);

    // If media is included in the request, create it automatically
    if (postDTO.getMedia() != null && !postDTO.getMedia().isEmpty()) {
      for (MediaDto mediaDto : postDTO.getMedia()) {
        Media media = mediaMapper.toMedia(mediaDto);
        media.setPost(savedPost);
        mediaRepository.save(media);
      }
    }

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

    post.setCaptions(postDto.getCaptions());
    post.setAudience(postDto.getAudience());
    post.setIsCommentVisible(postDto.getIsCommentVisible());
    post.setIsLikeVisible(postDto.getIsLikeVisible());

    Set<Media> currentMedia = post.getMedia();
    Set<MediaDto> updatedMediaDTOs = postDto.getMedia();

    // Extract URLs from updated DTOs
    Set<String> updatedUrls =
        updatedMediaDTOs.stream().map(MediaDto::url).collect(Collectors.toSet());

    // Identify and remove media that should no longer be associated
    currentMedia.removeIf(media -> !updatedUrls.contains(media.getUrl()));

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
    ;
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

  // @Override
  // public List<PostDto> getMyPosts(String username) {
  // return postRepository.getMyPosts(username);
  // }

  public List<PostDto> getMyPosts(String userId, Integer status, Audience audience) {
    return mapper.toPostDtoList(postRepository.findMyPosts(userId, status, audience));
  }

  public List<PostDto> getArchiveMyPosts(String userId, Integer status) {
    return mapper.toPostDtoList(postRepository.findArchiveMyPosts(userId, status));
  }

  public List<PostDto> getPostsByHashtag(String hashtag) {
    HashtagDto h = hashtagService.findByContent(hashtag);
    List<String> postIds = hashtagService.getPostIdsByHashtagId(h.id());
    List<PostDto> list = mapper.toPostDtoList(postRepository.findAllById(postIds));
    return list;
  }

  public List<PostDto> getRecommendedPosts(String userId) {
    // Logic recommendation: Lấy posts từ user follow, lượt like, hashtag, v.v.
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

  public PostFeedResponse getPersonalizedFeed(String userId, Pageable pageable) {

    Page<PersonalizedPostDto> personalizedPosts =
        postRepository.findPersonalizedPostsSimple(userId, pageable);

    List<PostDto> postDtos =
        personalizedPosts.getContent().stream()
            .map(
                dto -> {
                  PostDto postDTO = mapper.toPostDto(dto.post());
                  postDTO.setCommentCount(dto.commentCount());
                  return postDTO;
                })
            .collect(Collectors.toList());

    boolean hasNext = personalizedPosts.hasNext();

    if (postDtos.isEmpty()) {
      Page<PersonalizedPostDto> fallbackPosts =
          postRepository.findPersonalizedPostsCombined(
              userId, PageRequest.of(0, pageable.getPageSize()));

      postDtos =
          fallbackPosts.getContent().stream()
              .map(
                  dto -> {
                    PostDto postDTO = mapper.toPostDto(dto.post());
                    postDTO.setCommentCount(dto.commentCount());
                    return postDTO;
                  })
              .collect(Collectors.toList());

      hasNext = fallbackPosts.hasNext();
    }

    if (!hasNext || postDtos.size() < pageable.getPageSize()) {
      int targetSize = pageable.getPageSize();
      List<PostDto> pool = new ArrayList<>(postDtos);
      if (!pool.isEmpty()) {
        long seed = ((long) (userId == null ? 0 : userId.hashCode())) ^ pageable.getPageNumber();
        Collections.shuffle(pool, new Random(seed));
        List<PostDto> extended = new ArrayList<>(targetSize);
        for (int i = 0; i < targetSize; i++) {
          extended.add(pool.get(i % pool.size()));
        }
        postDtos = extended;
        hasNext = true;
      }
    }

    return new PostFeedResponse(postDtos, hasNext, pageable.getPageNumber());
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

  public Page<PostDto> getPostsWithFilters(
      String captions,
      Integer status,
      Audience audience,
      LocalDateTime postedAt,
      Boolean isCommentVisible,
      Boolean isLikeVisible,
      Set<HashtagDetailDto> hashtags,
      Long commentCount,
      String commentCountOperator,
      int page,
      int size) {

    Pageable pageable = PageRequest.of(page, size);

    Page<Object[]> filteredPosts =
        postRepository.findPostsWithFilters(
            captions,
            status,
            audience,
            postedAt,
            isCommentVisible,
            isLikeVisible,
            commentCount,
            commentCountOperator,
            pageable);

    return filteredPosts.map(
        result -> {
          // Since we're using native query, we need to manually map the result
          // The result contains all Post fields as Object[]
          Post post = new Post();
          post.setId((String) result[0]);
          post.setCaptions((String) result[1]);
          post.setStatus((Integer) result[2]);
          post.setAudience(Audience.valueOf((String) result[3]));
          post.setPostedAt((LocalDateTime) result[4]);
          post.setIsCommentVisible((Boolean) result[5]);
          post.setIsLikeVisible((Boolean) result[6]);
          post.setUpdatedAt((LocalDateTime) result[7]);
          // Note: user_id is at index 8, but we'll need to fetch the user separately if needed

          return mapper.toPostDto(post);
        });
  }

  public Page<PostDto> getPostsWithFilters(PostFilterDto filterDto, int page, int size) {
    return getPostsWithFilters(
        filterDto.captions(),
        filterDto.status(),
        filterDto.audience(),
        filterDto.postedAt(),
        filterDto.isCommentVisible(),
        filterDto.isLikeVisible(),
        filterDto.hashtags(),
        filterDto.commentCount(),
        filterDto.commentCountOperator(),
        page,
        size);
  }
}
