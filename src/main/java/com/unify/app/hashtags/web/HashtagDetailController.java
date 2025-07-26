package com.unify.app.hashtags.web;

import com.unify.app.hashtags.domain.HashtagDetailService;
import com.unify.app.hashtags.domain.models.HashtagDetailDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hashtag-details")
@RequiredArgsConstructor
class HashtagDetailController {

  private final HashtagDetailService service;

  @PostMapping("/saveAll")
  public List<HashtagDetailDto> saveAll(@RequestBody List<HashtagDetailDto> list) {
    return service.saveAll(list);
  }
}
