package com.unify.app.posts.domain;

import com.unify.app.hashtags.domain.HashtagService;
import com.unify.app.hashtags.domain.models.HashtagDto;
import com.unify.app.posts.domain.models.Audience;
import com.unify.app.posts.domain.models.MediaDto;
import com.unify.app.posts.domain.models.PersonalizedPostDto;
import com.unify.app.posts.domain.models.PostDto;
import com.unify.app.posts.domain.models.PostFeedResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

  private final PostRepository postRepository;
  private final PostMapper mapper;
  private final HashtagService hashtagService;

  @CacheEvict(value = "personalizedFeedCache", allEntries = true)
  public PostDto createPost(PostDto postDTO) {
    Post post = mapper.toPost(postDTO);
    postRepository.save(post);
    return mapper.toPostDto(post);
  }

  public List<PostDto> getAll() {
    return postRepository.findAll().stream().map(mapper::toPostDto).collect(Collectors.toList());
  }

  public PostDto getById(String id) {
    Post post =
        postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("Post not found!"));
    return mapper.toPostDto(post);
  }

  public Post findById(String id) {
    return postRepository
        .findById(id)
        .orElseThrow(() -> new PostNotFoundException("Post not found!"));
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

    System.out.println("posts" + personalizedPosts.getContent());

    // Convert to PostDto
    List<PostDto> postDtos =
        personalizedPosts.getContent().stream()
            .map(
                dto -> {
                  PostDto postDTO = mapper.toPostDto(dto.post());
                  postDTO.setCommentCount(dto.commentCount());
                  return postDTO;
                })
            .collect(Collectors.toList());

    return new PostFeedResponse(postDtos, personalizedPosts.hasNext(), pageable.getPageNumber());
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
}
