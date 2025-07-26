package com.unify.app.hashtags.web;

import com.unify.app.hashtags.domain.HashtagService;
import com.unify.app.hashtags.domain.models.HashtagDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hashtags")
@RequiredArgsConstructor
class HashtagController {

  private final HashtagService hashtagService;

  @PostMapping("/saveAll")
  public List<HashtagDto> saveAll(@RequestBody List<HashtagDto> hashtagDTOs) {
    return hashtagService.saveAll(hashtagDTOs);
  }
}
