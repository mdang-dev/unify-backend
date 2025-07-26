package com.unify.app.hashtags.domain;

import com.unify.app.hashtags.domain.models.HashtagDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HashtagMapper {

  HashtagDto toHashtagDTO(Hashtag hashtag);

  Hashtag toHashtag(HashtagDto hashtagDTO);

  List<Hashtag> toHashtagList(List<HashtagDto> dtoList);

  List<HashtagDto> toHashtagDTOList(List<Hashtag> hashtags);
}
