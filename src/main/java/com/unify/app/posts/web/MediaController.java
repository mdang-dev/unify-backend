package com.unify.app.posts.web;

import com.unify.app.posts.domain.MediaServie;
import com.unify.app.posts.domain.models.MediaDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
class MediaController {

  private final MediaServie mediaService;

  // @PostMapping
  // public MediaDTO save(MediaDTO mediaDTO) {
  // return mediaService.create(mediaDTO);
  // }

  @PostMapping
  public List<MediaDto> saveAll(@RequestBody List<MediaDto> mediaDTOs) {
    return mediaService.saveAllByPostId(mediaDTOs);
  }

  @GetMapping("/{postId}")
  public List<MediaDto> getMediaByPostId(@PathVariable("postId") String postId) {
    return mediaService.findByPostId(postId);
  }
}
