package com.unify.app.notifications.domain;

import com.unify.app.notifications.domain.models.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

interface NotificationRepository extends MongoRepository<Notification, String> {
  List<Notification> findByReceiverOrderByTimestampDesc(String receiver);

  Optional<Notification> findTopBySenderAndReceiverAndTypeOrderByTimestampDesc(
      String sender, String receiver, NotificationType type);

  void deleteBySenderAndReceiverAndType(String sender, String receiver, NotificationType type);
}
