package com.unify.app.messages.domain;

import com.unify.app.followers.domain.FollowService;
import com.unify.app.messages.domain.models.SharePostRequestDto;
import com.unify.app.messages.domain.models.SharePostResponseDto;
import com.unify.app.posts.domain.Post;
import com.unify.app.posts.domain.PostService;
import com.unify.app.security.SecurityService;
import com.unify.app.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ShareService {
  private final PostService postService;
  private final FollowService followService;
  private final SecurityService securityService;

  public SharePostResponseDto sharePost(SharePostRequestDto request) {

    Post post = postService.findById(request.postId());

    User postOwner = post.getUser();

    // Check friendship
    if (!followService.shouldBeFriends(securityService.getCurrentUserId(), postOwner.getId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You are not friends with the post owner");
    }

    // Generate share link (customize as needed)
    String shareLink = "/posts/" + post.getId();
    return new SharePostResponseDto(shareLink);
  }
}
