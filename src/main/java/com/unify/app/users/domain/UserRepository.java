package com.unify.app.users.domain;

import com.unify.app.users.domain.models.UserReportCountDto;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserRepository extends JpaRepository<User, String> {

  // == Basic Finders ==
  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  // == Password Update ==
  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
  void updatePasswordByEmail(@Param("email") String email, @Param("password") String password);

  // == Suggested Friends with Mutual Friends ==
  @Query(
      """
                        SELECT DISTINCT u FROM User u
                        LEFT JOIN u.roles r ON r.name = 'ADMIN'
                        WHERE u.id <> :currentUserId
                        AND r.id IS NULL
                        AND NOT EXISTS (
                        SELECT 1 FROM Follower fo
                        WHERE fo.userFollowing.id = u.id
                        AND fo.userFollower.id = :currentUserId
                        )
                        AND EXISTS (
                        SELECT 1 FROM Friendship f1
                        WHERE (
                        (f1.user.id = :currentUserId OR f1.friend.id = :currentUserId)
                        AND f1.friendshipStatus = 'ACCEPTED'
                        AND EXISTS (
                        SELECT 1 FROM Friendship f2
                        WHERE (
                        (f2.user.id = u.id OR f2.friend.id = u.id)
                        AND f2.friendshipStatus = 'ACCEPTED'
                        AND (
                        f1.user.id = f2.user.id OR f1.user.id = f2.friend.id OR
                        f1.friend.id = f2.user.id OR f1.friend.id = f2.friend.id
                        )
                        )
                        )
                        )
                        )
                        """)
  List<User> findSuggestedFriendsWithMutualFriends(
      @Param("currentUserId") String currentUserId, Pageable pageable);

  // // == Suggested Strangers (no mutual required) ==
  @Query(
      """
                        SELECT DISTINCT u FROM User u
                        LEFT JOIN u.roles r ON r.name = 'ADMIN'
                        WHERE u.id <> :currentUserId
                        AND r.id IS NULL
                        AND NOT EXISTS (
                        SELECT 1 FROM Follower fo
                        WHERE fo.userFollowing.id = u.id
                        AND fo.userFollower.id = :currentUserId
                        )
                        """)
  List<User> findSuggestedStrangers(
      @Param("currentUserId") String currentUserId, Pageable pageable);

  // == Suggested Strangers Excluding Existing IDs ==
  @Query(
      """
                        SELECT DISTINCT u FROM User u
                        LEFT JOIN u.roles r ON r.name = 'ADMIN'
                        WHERE u.id <> :currentUserId
                        AND r.id IS NULL
                        AND NOT EXISTS (
                        SELECT 1 FROM Follower fo
                        WHERE fo.userFollowing.id = u.id
                        AND fo.userFollower.id = :currentUserId
                        )
                        AND u.id NOT IN :excludedIds
                        """)
  List<User> findSuggestedStrangersExcluding(
      @Param("currentUserId") String currentUserId,
      @Param("excludedIds") List<String> excludedIds,
      Pageable pageable);

  // == Following ==
  @Query("SELECT fo.userFollowing FROM Follower fo WHERE fo.userFollower.id = :currentUserId")
  List<User> findUsersFollowedBy(@Param("currentUserId") String currentUserId);

  // == Followers ==
  @Query("SELECT fo.userFollower FROM Follower fo WHERE fo.userFollowing.id = :currentUserId")
  List<User> findUsersFollowingMe(@Param("currentUserId") String currentUserId);

  // // == Friends ==
  @Query(
      """
                        SELECT DISTINCT u FROM User u
                        JOIN Friendship f ON (f.user.id = u.id OR f.friend.id = u.id)
                        WHERE (f.user.id = :currentUserId OR f.friend.id = :currentUserId)
                        AND f.friendshipStatus = 'ACCEPTED'
                        AND u.id <> :currentUserId
                        """)
  List<User> findFriendsByUserId(@Param("currentUserId") String currentUserId);

  // == Users by Role ==
  @Query(
      """
                        SELECT u FROM User u
                        JOIN u.roles r
                        WHERE r.id = 2
                        """)
  List<User> findAllUserByRole(); // Consider replacing hardcoded `2` with a

  // param or Enum

  // == Report Count ==
  @Query(
      """
                        SELECT new com.unify.app.users.domain.models.UserReportCountDto(u.id, u.username,
                        u.email, COUNT(r))
                        FROM User u
                        JOIN u.roles role
                        LEFT JOIN Report r ON r.reportedId = u.id AND r.status = 1
                        WHERE role.id = 2
                        GROUP BY u.id, u.username, u.email
                        """)
  List<UserReportCountDto> findAllUserAndCountReportByRole(); // Also consider

  // param for role

  // == Search by Name or Username ==
  List<User>
      findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
          String username, String firstName, String lastName);

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
