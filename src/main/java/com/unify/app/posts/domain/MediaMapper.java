package com.unify.app.posts.domain;

import com.unify.app.posts.domain.models.MediaDto;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MediaMapper {

  Media toMedia(MediaDto mediaDTO);

  MediaDto toMediaDTO(Media media);

  Set<MediaDto> toSetOfMediaDTO(Set<Media> media);
}
