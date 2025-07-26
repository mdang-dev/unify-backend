package com.unify.app.followers.domain;

import com.unify.app.followers.domain.models.FriendshipUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface FriendshipRepository extends JpaRepository<Friendship, FriendshipUserId> {

  @Query(
      "SELECT COUNT(f) > 0 FROM Friendship f WHERE ((f.user.id = :userId1 AND f.friend.id = :userId2) OR (f.user.id = :userId2 AND f.friend.id = :userId1)) AND f.friendshipStatus = com.unify.app.followers.domain.models.FriendshipStatus.ACCEPTED")
  boolean areFriends(@Param("userId1") String userId1, @Param("userId2") String userId2);
}
