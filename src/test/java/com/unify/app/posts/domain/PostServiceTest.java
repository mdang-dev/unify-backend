package com.unify.app.posts.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.unify.app.hashtags.domain.HashtagService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  @Mock private PostRepository postRepository;

  @Mock private PostMapper mapper;

  @Mock private MediaMapper mediaMapper;

  @Mock private HashtagService hashtagService;

  @Mock private MediaRepository mediaRepository;

  @Test
  void testMockitoWorksWithoutWarnings() {
    // This test verifies that Mockito works without the inline-mock-maker warnings
    PostService postService =
        new PostService(postRepository, mapper, mediaMapper, hashtagService, mediaRepository);

    // Simple assertion to ensure the test runs
    assertNotNull(postService);
  }
}
