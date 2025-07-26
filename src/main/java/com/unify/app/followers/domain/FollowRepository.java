package com.unify.app.followers.domain;

import com.unify.app.followers.domain.models.FollowerUserId;
import com.unify.app.users.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface FollowRepository extends JpaRepository<Follower, FollowerUserId> {

  long countByUserFollowingId(
      String followingId); // Count how many users the specified user is following

  long countByUserFollowerId(String followerId); // Count how many users follow the specified user

  // Users that the current user is following
  @Query(
      "SELECT fo.userFollowing FROM Follower fo "
          + "WHERE fo.userFollower.username = :currentUsername")
  List<User> findUsersFollowedBy(@Param("currentUsername") String currentUsername);

  // Users that are following the current user
  @Query(
      "SELECT fo.userFollower FROM Follower fo "
          + "WHERE fo.userFollowing.username = :currentUsername")
  List<User> findUsersFollowingMe(@Param("currentUsername") String currentUsername);

  // Users that follow each other (mutual follows)
  @Query(
      """
                        SELECT f.userFollowing
                        FROM Follower f
                        WHERE f.userFollower.id = :myId
                        AND f.userFollowing.id IN (
                            SELECT f2.userFollower.id
                            FROM Follower f2
                            WHERE f2.userFollowing.id = :myId
                        )
                        """)
  List<User> findMutualFollowingUsers(@Param("myId") String myId);
}
