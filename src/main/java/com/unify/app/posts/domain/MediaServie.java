package com.unify.app.posts.domain;

import com.unify.app.posts.domain.models.MediaDto;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaServie {

  private final MediaRepository mediaRepository;
  private final MediaMapper mapper;

  public MediaDto update(MediaDto mediaDTO) {
    Media media =
        mediaRepository
            .findById(mediaDTO.id())
            .orElseThrow(() -> new MediaNotFoundException("Media not found!"));
    media = mediaRepository.save(mapper.toMedia(mediaDTO));
    return mapper.toMediaDTO(media);
  }

  public MediaDto findById(String id) {
    Media media =
        mediaRepository
            .findById(id)
            .orElseThrow(() -> new MediaNotFoundException("Media not found!"));
    return mapper.toMediaDTO(media);
  }

  public List<MediaDto> findAll() {
    return mediaRepository.findAll().stream().map(mapper::toMediaDTO).collect(Collectors.toList());
  }

  public void deleteById(String id) {
    mediaRepository.deleteById(id);
  }

  public List<MediaDto> findByPostId(String postId) {
    List<Media> mediaList = mediaRepository.findByPostId(postId);

    return mediaList.stream().map(mapper::toMediaDTO).collect(Collectors.toList());
  }

  public List<MediaDto> saveAllByPostId(List<MediaDto> mediaDTOs) {
    List<Media> mediaList = new ArrayList<>();

    for (MediaDto mediaDTO : List.copyOf(mediaDTOs)) {
      Media media = mapper.toMedia(mediaDTO);
      mediaList.add(media);
    }

    List<Media> savedMedia = mediaRepository.saveAll(mediaList);

    return savedMedia.stream().map(mapper::toMediaDTO).collect(Collectors.toList());
  }
}
