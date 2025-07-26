package com.unify.app.messages.domain.models;

import com.unify.app.users.domain.models.AvatarDto;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChatDto implements Serializable {
  String userId;
  String username;
  String fullName;
  AvatarDto avatar;
  String lastMessage;
  LocalDateTime lastMessageTime;
}
