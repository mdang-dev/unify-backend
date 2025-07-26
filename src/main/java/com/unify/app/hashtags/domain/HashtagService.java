package com.unify.app.hashtags.domain;

import com.unify.app.hashtags.domain.models.HashtagDto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class HashtagService {

  private final HashtagMapper mapper;
  private final HashtagRepository hashtagRepository;
  private final HashtagDetailRepository hashtagDetailRepository;

  public HashtagDto createHashtag(HashtagDto hashtagDTO) {
    Hashtag hashtag = mapper.toHashtag(hashtagDTO);
    hashtagRepository.save(hashtag);
    return mapper.toHashtagDTO(hashtag);
  }

  public HashtagDto findByContent(String content) {
    return hashtagRepository
        .findByContent(content)
        .map(mapper::toHashtagDTO)
        .orElseThrow(
            () -> new HashtagNotFoundException("Hash tags not found with content " + content));
  }

  public List<String> getPostIdsByHashtagId(String id) {
    return hashtagDetailRepository.findPostByHashtagId(id);
  }

  public List<HashtagDto> getAll() {
    return hashtagRepository.findAll().stream()
        .map(mapper::toHashtagDTO)
        .collect(Collectors.toList());
  }

  public HashtagDto getById(String id) {
    Hashtag hashtag =
        hashtagRepository
            .findById(id)
            .orElseThrow(() -> new HashtagNotFoundException("Hashtag not found!"));
    return mapper.toHashtagDTO(hashtag);
  }

  public HashtagDto updateHashtag(HashtagDto hashtagDTO) {
    Hashtag hashtag =
        hashtagRepository.save(
            hashtagRepository
                .findById(hashtagDTO.id())
                .orElseThrow(() -> new HashtagNotFoundException("Hashtag not found!")));
    return mapper.toHashtagDTO(hashtag);
  }

  public void deletePostById(String id) {
    hashtagRepository.deleteById(id);
  }

  @Transactional
  public List<HashtagDto> saveAll(List<HashtagDto> hashtagDTOs) {
    List<Hashtag> hashtags =
        hashtagDTOs.stream()
            .map(
                (HashtagDto dto) -> {
                  Optional<Hashtag> optional = hashtagRepository.findByContent(dto.content());
                  return optional.orElseGet(() -> mapper.toHashtag(dto));
                })
            .collect(Collectors.toList());
    List<Hashtag> savedHashtags = hashtagRepository.saveAll(hashtags);
    return savedHashtags.stream().map(mapper::toHashtagDTO).collect(Collectors.toList());
  }
}
