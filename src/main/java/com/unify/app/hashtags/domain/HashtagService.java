package com.unify.app.hashtags.domain;

import com.unify.app.hashtags.domain.models.HashtagDetailDto;
import com.unify.app.hashtags.domain.models.HashtagDto;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class HashtagService {

  private final HashtagMapper mapper;
  private final HashtagDetailMapper detailMapper;
  private final HashtagRepository hashtagRepository;
  private final HashtagDetailRepository hashtagDetailRepository;

  @Transactional
  public List<HashtagDto> saveAll(List<HashtagDto> hashtagDTOs) {
    List<Hashtag> hashtags = mapper.toHashtagList(hashtagDTOs);
    List<Hashtag> savedHashtags = hashtagRepository.saveAll(hashtags);
    return mapper.toHashtagDTOList(savedHashtags);
  }

  public Optional<HashtagDto> findByContent(String content) {
    return hashtagRepository.findByContent(content).map(mapper::toHashtagDTO);
  }

  public List<String> getPostIdsByHashtagId(String id) {
    return hashtagRepository.findPostByHashtagId(id);
  }

  @Transactional
  public List<HashtagDetailDto> saveAllDetails(List<HashtagDetailDto> hashtagDetailDTOs) {
    List<HashtagDetail> hashtagDetails = detailMapper.toHashtagDetailList(hashtagDetailDTOs);
    List<HashtagDetail> savedDetails = hashtagDetailRepository.saveAll(hashtagDetails);
    return detailMapper.toHashtagDetailDTOList(savedDetails);
  }
}
