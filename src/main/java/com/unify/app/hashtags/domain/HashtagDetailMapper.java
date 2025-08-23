package com.unify.app.hashtags.domain;

import com.unify.app.hashtags.domain.models.HashtagDetailDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HashtagDetailMapper {

  @Mapping(target = "postId", source = "post.id")
  @Mapping(target = "hashtagId", source = "hashtag.id")
  HashtagDetailDto toHashtagDetailDTO(HashtagDetail hashtagDetail);

  @Mapping(target = "post.id", source = "postId")
  @Mapping(target = "hashtag.id", source = "hashtagId")
  HashtagDetail toHashtagDetail(HashtagDetailDto hashtagDetailDTO);

  List<HashtagDetail> toHashtagDetailList(List<HashtagDetailDto> dtoList);

  List<HashtagDetailDto> toHashtagDetailDTOList(List<HashtagDetail> hashtags);
}
