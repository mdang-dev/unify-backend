package com.unify.app.notifications.domain;

import com.unify.app.notifications.domain.models.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

interface NotificationRepository extends MongoRepository<Notification, String> {
  List<Notification> findByReceiverOrderByTimestampDesc(String receiver);

  // ✅ NEW: Paginated notifications
  Page<Notification> findByReceiverOrderByTimestampDesc(String receiver, Pageable pageable);

  // ✅ NEW: Count unread notifications
  @Query("{'receiver': ?0, 'isRead': false}")
  long countByReceiverAndIsReadFalse(String receiver);

  // ✅ NEW: Find unread notifications count by type
  @Query("{'receiver': ?0, 'type': ?1, 'isRead': false}")
  long countByReceiverAndTypeAndIsReadFalse(String receiver, NotificationType type);

  Optional<Notification> findTopBySenderAndReceiverAndTypeOrderByTimestampDesc(
      String sender, String receiver, NotificationType type);

  void deleteBySenderAndReceiverAndType(String sender, String receiver, NotificationType type);
}
