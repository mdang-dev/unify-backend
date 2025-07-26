package com.unify.app.hashtags.domain;

import com.unify.app.hashtags.domain.models.HashtagDetailDto;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HashtagDetailService {

  private final HashtagDetailRepository hashtagDetailRepository;
  private final HashtagDetailMapper mapper;

  @Transactional
  public List<HashtagDetailDto> saveAll(List<HashtagDetailDto> hashtagDetailDTOs) {
    List<HashtagDetail> hashtags = mapper.toHashtagDetailList(hashtagDetailDTOs);
    List<HashtagDetail> savedHashtag = hashtagDetailRepository.saveAll(hashtags);
    return mapper.toHashtagDetailDTOList(savedHashtag);
  }
}
