package com.unify.app.media.domain;

import com.unify.app.media.domain.models.UserLiveStatusDto;
import com.unify.app.users.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
      JOIN followers f ON f.following_id = u.id
      WHERE s.is_live = TRUE
        AND f.follower_id = :userId
      """,
      nativeQuery = true)
  List<User> findLiveUsersFollowedBy(@Param("userId") String userId);

    @Query(value = """
    SELECT s.*
    FROM streams s
    LEFT JOIN followers f 
           ON f.following_id = s.user_id 
          AND f.follower_id = :viewerId
    WHERE s.is_live = true
    ORDER BY 
      CASE WHEN f.follower_id IS NOT NULL THEN 0 ELSE 1 END,
      f.follower_id IS NOT NULL DESC,
      s.start_time DESC,
      RANDOM()
    """,
            nativeQuery = true)
    Page<Stream> findLiveStreamsWithFollowPriorityAndRandomNonFollow(
            @Param("viewerId") String viewerId,
            Pageable pageable
    );

    @Query("""
    SELECT new com.unify.app.media.domain.models.UserLiveStatusDto(
                                        u.id,
                                        u.firstName,
                                        u.lastName,
                                        u.username,
                                        CASE WHEN s.id IS NOT NULL THEN true ELSE false END,
                                        (
                                            SELECT a.url
                                            FROM Avatar a
                                            WHERE a.user.id = u.id
                                            AND a.createdAt = (
                                                SELECT MAX(a2.createdAt)
                                                FROM Avatar a2
                                                WHERE a2.user.id = u.id
                                            )
                                        )
                                )
                                FROM User u
                                JOIN Follower f ON f.userFollowing.id = u.id
                                LEFT JOIN Stream s ON s.user.id = u.id AND s.isLive = true
                                WHERE f.userFollower.id = :viewerId
                                ORDER BY
                                  CASE WHEN s.id IS NOT NULL THEN 0 ELSE 1 END,
                                  s.startTime DESC NULLS LAST,
                                  u.username ASC
            
""")
    Page<UserLiveStatusDto> findFollowedUsersWithLivePriority(
            @Param("viewerId") String viewerId,
            Pageable pageable
    );






}
