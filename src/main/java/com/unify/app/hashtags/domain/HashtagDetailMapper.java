package com.unify.app.hashtags.domain;

import com.unify.app.hashtags.domain.models.HashtagDetailDto;
import com.unify.app.posts.domain.PostMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {PostMapper.class})
public interface HashtagDetailMapper {

  HashtagDetailDto toHashtagDetailDTO(HashtagDetail hashtagDetail);

  HashtagDetail toHashtagDetail(HashtagDetailDto hashtagDetailDTO);

  List<HashtagDetail> toHashtagDetailList(List<HashtagDetailDto> dtoList);

  List<HashtagDetailDto> toHashtagDetailDTOList(List<HashtagDetail> hashtags);
}
