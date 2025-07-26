package com.unify.app.messages.web;

import com.unify.app.messages.domain.ShareService;
import com.unify.app.messages.domain.models.SharePostRequestDto;
import com.unify.app.messages.domain.models.SharePostResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shares")
@RequiredArgsConstructor
class ShareController {
  private final ShareService shareService;

  @PostMapping
  public ResponseEntity<SharePostResponseDto> sharePost(@RequestBody SharePostRequestDto request) {
    SharePostResponseDto response = shareService.sharePost(request);
    return ResponseEntity.ok(response);
  }
}
