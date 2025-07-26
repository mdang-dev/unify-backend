package com.unify.app.media.domain;

import com.unify.app.users.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface StreamRepository extends JpaRepository<Stream, String> {
  Optional<Stream> findByRoomId(String roomId);

  List<Stream> findByIsLive(Boolean isLive);

  Optional<Stream> findByIngressId(String ingressId);

  Optional<Stream> findByUserId(String userId);

  @Query(
      value =
          """
                SELECT u.*
                FROM users u
                JOIN streams s ON u.id = s.user_id
                JOIN follows f ON f.following_id = u.id
                WHERE s.is_live = TRUE
                  AND f.follower_id = :userId
            """,
      nativeQuery = true)
  List<User> findLiveUsersFollowedBy(@Param("userId") String userId);
}
