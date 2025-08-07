package com.unify.app.media.domain.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CallSession {
  private String token;
  private boolean video;
  private boolean isCaller;
  private String calleeName;
  private String calleeAvatar;
  private String room;
  private String userId;
}
