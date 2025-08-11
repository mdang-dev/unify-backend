package com.unify.app.notifications.domain;

import com.unify.app.notifications.domain.models.NotificationDto;
import com.unify.app.users.domain.Avatar;
import com.unify.app.users.domain.User;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class NotificationMapper {
  public NotificationDto toNotificationDTO(Notification notification, Map<String, User> userMap) {
    User sender = userMap.get(notification.getSender());

    NotificationDto.SenderDto senderDTO =
        sender != null
            ? NotificationDto.SenderDto.builder()
                .id(sender.getId())
                .fullName(sender.getFirstName() + " " + sender.getLastName())
                .avatar(getAvatarUrl(sender.latestAvatar()))
                .build()
            : null;

    return NotificationDto.builder()
        .id(notification.getId())
        .sender(senderDTO)
        .message(notification.getMessage())
        .type(notification.getType())
        .timestamp(notification.getTimestamp())
        .isRead(notification.isRead())
        .link(notification.getLink())
        .data(notification.getData())
        .build();
  }

  private String getAvatarUrl(Avatar avatar) {
    return avatar != null ? avatar.getUrl() : null;
  }
}
