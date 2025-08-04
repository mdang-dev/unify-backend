package com.unify.app.posts.saved;

import com.unify.app.posts.domain.Post;
import com.unify.app.posts.domain.PostMapper;
import com.unify.app.posts.domain.PostService;
import com.unify.app.posts.domain.models.PostDto;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.UserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedPostService {
  private final SavedPostRepository savedPostRepository;
  private final UserService userService;
  private final PostService postService;
  private final PostMapper postMapper;
  private final SavedPostMapper savedPostMapper;

  public void toggleSavePost(String userId, String postId) {
    if (savedPostRepository.existsByUserIdAndPostId(userId, postId)) {
      savedPostRepository.deleteByUserIdAndPostId(userId, postId);
    } else {
      savePost(userId, postId);
    }
  }

  public void savePost(String userId, String postId) {
    if (savedPostRepository.existsByUserIdAndPostId(userId, postId)) {
      throw new IllegalStateException("Post is already saved.");
    }

    User user = userService.findUserById(userId);
    Post post = postService.findById(postId);

    SavedPost savedPost = SavedPost.builder().user(user).post(post).build();

    savedPostRepository.save(savedPost);
  }

  public void unsavePostById(String id) {
    if (!savedPostRepository.existsById(id)) {
      throw new IllegalStateException("Saved post not found.");
    }
    savedPostRepository.deleteById(id);
  }

  public List<SavedPostDto> getSavedPostsByUsername(String username) {
    User user = userService.findUserByUsername(username);
    return savedPostRepository.findPublicSavedPostsByUserIdOrderBySavedAtDesc(user.getId()).stream()
        .map(savedPostMapper::toSavedPostDTO)
        .collect(Collectors.toList());
  }

  public List<PostDto> getSavedPostDetails(String postId) {
    List<SavedPost> savedPosts = savedPostRepository.findByPostId(postId);
    return savedPosts.stream()
        .map(savedPost -> postMapper.toPostDto(savedPost.getPost()))
        .collect(Collectors.toList());
  }

  public boolean isPostSaved(String userId, String postId) {
    return savedPostRepository.existsByUserIdAndPostId(userId, postId);
  }
}
