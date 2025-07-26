package com.unify.app.posts.liked;

import com.unify.app.posts.domain.PostMapper;
import com.unify.app.posts.domain.PostService;
import com.unify.app.posts.domain.models.PostDto;
import com.unify.app.posts.liked.models.LikedPostRequest;
import com.unify.app.users.domain.UserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikedPostService {

  private final PostService postService;
  private final UserService userService;
  private final LikedPostRepository likedPostRepository;
  private final PostMapper postMapper;

  public List<PostDto> getListLikedPosts(String userId) {
    List<LikedPost> likedPosts = likedPostRepository.findAllByUserId(userId);
    return likedPosts.stream()
        .map(LikedPost::getPost)
        .map(postMapper::toPostDto)
        .collect(Collectors.toList());
  }

  public boolean checkLiked(String userId, String postId) {
    return likedPostRepository.existsByUserIdAndPostId(userId, postId);
  }

  public void createLikedPost(LikedPostRequest request) {
    LikedPost likedPost =
        LikedPost.builder()
            .post(postService.findById(request.postId()))
            .user(userService.findUserById(request.userId()))
            .build();
    likedPostRepository.save(likedPost);
  }

  public void deleteLikedPost(LikedPostRequest request) {

    LikedPost likedPost =
        likedPostRepository.findByUserIdAndPostId(request.userId(), request.postId());
    if (likedPost == null) {
      throw new IllegalStateException("No liked post found for this user and post");
    }
    likedPostRepository.deleteByUserIdAndPostId(request.userId(), request.postId());
  }

  public int countLikePost(String postId) {
    return likedPostRepository.countByPostId(postId);
  }
}
