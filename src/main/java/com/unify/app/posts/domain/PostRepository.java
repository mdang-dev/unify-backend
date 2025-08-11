package com.unify.app.posts.domain;

import com.unify.app.posts.domain.models.Audience;
import com.unify.app.posts.domain.models.PersonalizedPostDto;
import com.unify.app.posts.domain.models.PostDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PostRepository extends JpaRepository<Post, String> {

  @Query(
      """
                SELECT p, (COUNT(DISTINCT lp.id) + COUNT(DISTINCT pc.id)) AS interactionCount
                FROM Post p
                LEFT JOIN p.likedPosts lp
                LEFT JOIN p.comments pc
                WHERE p.status != 2
                GROUP BY p
                ORDER BY interactionCount DESC
            """)
  List<Object[]> findPostsWithInteractionCounts();

  @Query(
      """
                SELECT p, COUNT(DISTINCT pc.id) AS commentCount,
                       (COUNT(DISTINCT lp.id) + COUNT(DISTINCT pc.id)) AS interactionCount
                FROM Post p
                LEFT JOIN p.likedPosts lp
                LEFT JOIN p.comments pc
                WHERE p.user.id NOT IN (
                    SELECT f.userFollowing.id FROM Follower f WHERE f.userFollower.id = :userId
                )
                AND p.user.id != :userId
                AND p.status != 2
                GROUP BY p
                ORDER BY interactionCount DESC
            """)
  List<Object[]> findPostsWithInteractionCountsAndNotFollow(@Param("userId") String userId);

  @Query("FROM Post o WHERE o.postedAt BETWEEN :start AND :end")
  List<Post> getPostsByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("FROM Post o WHERE o.user.username = ?1")
  List<PostDto> getMyPosts(String username);

  @Query(
      "SELECT p FROM Post p WHERE p.user.id = :userId AND p.status = :status AND p.audience = :audience ORDER BY p.postedAt DESC")
  List<Post> findMyPosts(
      @Param("userId") String userId,
      @Param("status") Integer status,
      @Param("audience") Audience audience);

  @Query(
      "SELECT p FROM Post p WHERE p.user.id = :userId AND p.status = :status ORDER BY p.postedAt DESC")
  List<Post> findArchiveMyPosts(@Param("userId") String userId, @Param("status") Integer status);

  @Query("SELECT p FROM Post p WHERE p.user.id = :userId ORDER BY p.postedAt DESC")
  List<Post> findPostsByUserId(@Param("userId") String userId);

  @Override
  Optional<Post> findById(String id);

  @Query(
      """
                SELECT new com.unify.app.posts.domain.models.PersonalizedPostDto(
                    p,
                    COUNT(DISTINCT lp.id) + COUNT(DISTINCT pc.id),
                    COUNT(DISTINCT pc.id)
                )
                FROM Post p
                LEFT JOIN p.likedPosts lp
                LEFT JOIN p.comments pc
                WHERE p.status != 2
                AND p.user.id IN (
                    SELECT f.userFollowing.id FROM Follower f WHERE f.userFollower.id = :userId
                )
                GROUP BY p
                ORDER BY p.postedAt DESC
            """)
  Page<PersonalizedPostDto> findPersonalizedPostsFromFollowing(
      @Param("userId") String userId, Pageable pageable);

  @Query(
      """
                SELECT new com.unify.app.posts.domain.models.PersonalizedPostDto(
                    p,
                    COUNT(DISTINCT lp.id) + COUNT(DISTINCT pc.id),
                    COUNT(DISTINCT pc.id)
                )
                FROM Post p
                LEFT JOIN p.likedPosts lp
                LEFT JOIN p.comments pc
                WHERE p.status != 2
                AND p.user.id NOT IN (
                    SELECT f.userFollowing.id FROM Follower f WHERE f.userFollower.id = :userId
                )
                AND p.user.id != :userId
                GROUP BY p
                ORDER BY COUNT(DISTINCT lp.id) + COUNT(DISTINCT pc.id) DESC, p.postedAt DESC
            """)
  Page<PersonalizedPostDto> findPersonalizedPostsFromOthers(
      @Param("userId") String userId, Pageable pageable);

  @Query(
      """
                SELECT new com.unify.app.posts.domain.models.PersonalizedPostDto(
                    p,
                    COUNT(DISTINCT lp.id) + COUNT(DISTINCT pc.id),
                    COUNT(DISTINCT pc.id)
                )
                FROM Post p
                LEFT JOIN p.likedPosts lp
                LEFT JOIN p.comments pc
                WHERE p.status != 2
                AND p.user.id != :userId
                GROUP BY p
                ORDER BY
                    CASE WHEN p.user.id IN (
                        SELECT f.userFollowing.id FROM Follower f WHERE f.userFollower.id = :userId
                    ) THEN 0 ELSE 1 END,
                    p.postedAt DESC,
                    COUNT(DISTINCT lp.id) + COUNT(DISTINCT pc.id) DESC
            """)
  Page<PersonalizedPostDto> findPersonalizedPostsCombined(
      @Param("userId") String userId, Pageable pageable);

  @Query(
      """
                SELECT new com.unify.app.posts.domain.models.PersonalizedPostDto(
                    p,
                    COUNT(DISTINCT lp.id) + COUNT(DISTINCT pc.id),
                    COUNT(DISTINCT pc.id)
                )
                FROM Post p
                LEFT JOIN p.likedPosts lp
                LEFT JOIN p.comments pc
                WHERE p.status = 1
                  AND p.user.id != :userId
                GROUP BY p
                ORDER BY
                  CASE WHEN p.user.id IN (
                      SELECT f.userFollowing.id FROM Follower f WHERE f.userFollower.id = :userId
                  ) THEN 0 ELSE 1 END,
                  CASE WHEN p.user.id NOT IN (
                      SELECT f2.userFollowing.id FROM Follower f2 WHERE f2.userFollower.id = :userId
                  ) THEN function('random') ELSE 1 END,
                  p.postedAt DESC,
                  COUNT(DISTINCT lp.id) + COUNT(DISTINCT pc.id) DESC
            """)
  Page<PersonalizedPostDto> findPersonalizedPostsSimple(
      @Param("userId") String userId, Pageable pageable);

  @Query(
      """
                SELECT p, COUNT(pc)
                FROM Post p
                LEFT JOIN p.comments pc
                WHERE p.status != 2
                GROUP BY p
            """)
  List<Object[]> findPostsWithCommentCount();

  @Query(
      """
                SELECT p, COUNT(pc)
                FROM Post p
                LEFT JOIN p.comments pc
                WHERE p.id = :postId AND p.status != 2
                GROUP BY p
            """)
  Object[] findPostWithCommentCountById(@Param("postId") String postId);

  @Query(
      value =
          """
                SELECT p, COUNT(c) as commentCount
                FROM Post p
                JOIN p.media m
                LEFT JOIN p.comments c
                WHERE m.mediaType = 'VIDEO' AND p.status != 2
                GROUP BY p
            """,
      countQuery =
          """
                SELECT COUNT(p)
                FROM Post p
                JOIN p.media m
                WHERE m.mediaType = 'VIDEO' AND p.status != 2
            """)
  Page<Object[]> findReelsPostsWithCommentCount(Pageable pageable);

  @Query(
      value =
          """
                SELECT p.* FROM Posts p
                LEFT JOIN comments c ON p.id = c.post_id
                WHERE (:captions IS NULL OR LOWER(CAST(p.captions AS TEXT)) LIKE LOWER(CONCAT('%', :captions, '%')))
                AND (:status IS NULL OR p.status = :status)
                AND (:audience IS NULL OR p.audience = :audience)
                AND (:isCommentVisible IS NULL OR p.is_comment_visible = CAST(:isCommentVisible AS boolean))
                AND (:isLikeVisible IS NULL OR p.is_like_visible = CAST(:isLikeVisible AS boolean))
                GROUP BY p.id, p.captions, p.status, p.audience, p.posted_at, p.is_comment_visible, p.is_like_visible, p.updated_at, p.user_id
                HAVING (:commentCount IS NULL OR
                    CASE
                        WHEN :commentCountOperator = '>' THEN COUNT(c.id) > :commentCount
                        WHEN :commentCountOperator = '<' THEN COUNT(c.id) < :commentCount
                        WHEN :commentCountOperator = '=' THEN COUNT(c.id) = :commentCount
                        WHEN :commentCountOperator = '>=' THEN COUNT(c.id) >= :commentCount
                        WHEN :commentCountOperator = '<=' THEN COUNT(c.id) <= :commentCount
                        ELSE TRUE
                    END)
                ORDER BY p.posted_at DESC
                LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
            """,
      countQuery =
          """
                SELECT COUNT(DISTINCT p.id) FROM Posts p
                WHERE (:captions IS NULL OR LOWER(CAST(p.captions AS TEXT)) LIKE LOWER(CONCAT('%', :captions, '%')))
                AND (:status IS NULL OR p.status = :status)
                AND (:audience IS NULL OR p.audience = :audience)
                AND (:isCommentVisible IS NULL OR p.is_comment_visible = CAST(:isCommentVisible AS boolean))
                AND (:isLikeVisible IS NULL OR p.is_like_visible = CAST(:isLikeVisible AS boolean))
                AND (:commentCount IS NULL OR
                    CASE
                        WHEN :commentCountOperator = '>' THEN (SELECT COUNT(*) FROM comments WHERE post_id = p.id) > :commentCount
                        WHEN :commentCountOperator = '<' THEN (SELECT COUNT(*) FROM comments WHERE post_id = p.id) < :commentCount
                        WHEN :commentCountOperator = '=' THEN (SELECT COUNT(*) FROM comments WHERE post_id = p.id) = :commentCount
                        WHEN :commentCountOperator = '>=' THEN (SELECT COUNT(*) FROM comments WHERE post_id = p.id) >= :commentCount
                        WHEN :commentCountOperator = '<=' THEN (SELECT COUNT(*) FROM comments WHERE post_id = p.id) <= :commentCount
                        ELSE TRUE
                    END)
            """,
      nativeQuery = true)
  Page<Object[]> findPostsWithFilters(
      @Param("captions") String captions,
      @Param("status") Integer status,
      @Param("audience") Audience audience,
      @Param("isCommentVisible") Boolean isCommentVisible,
      @Param("isLikeVisible") Boolean isLikeVisible,
      @Param("commentCount") Long commentCount,
      @Param("commentCountOperator") String commentCountOperator,
      Pageable pageable);

  @Query(
      value =
          """
                SELECT DISTINCT p.id, p.captions, p.status, p.audience, p.posted_at,
                       p.is_comment_visible, p.is_like_visible, p.updated_at, p.user_id,
                       u.first_name, u.last_name, u.user_name, COUNT(c.id) as comment_count
                FROM Posts p
                LEFT JOIN users u ON p.user_id = u.id
                LEFT JOIN comments c ON p.id = c.post_id
                LEFT JOIN hashtag_details hd ON p.id = hd.post_id
                LEFT JOIN Hashtags h ON hd.hashtag_id = h.id
                WHERE (:captions IS NULL OR LOWER(CAST(p.captions AS TEXT)) LIKE LOWER(CONCAT('%', :captions, '%')))
                AND (:status IS NULL OR p.status = :status)
                AND (:audience IS NULL OR p.audience = :audience)
                AND (:isCommentVisible IS NULL OR p.is_comment_visible = CAST(:isCommentVisible AS boolean))
                AND (:isLikeVisible IS NULL OR p.is_like_visible = CAST(:isLikeVisible AS boolean))
                AND (:hashtag IS NULL OR LOWER(h.content) LIKE LOWER(CONCAT('%', :hashtag, '%')))
                GROUP BY p.id, p.captions, p.status, p.audience, p.posted_at,
                         p.is_comment_visible, p.is_like_visible, p.updated_at, p.user_id,
                         u.first_name, u.last_name, u.user_name
                HAVING (:commentCount IS NULL OR
                    CASE
                        WHEN :commentCountOperator = '>' THEN COUNT(c.id) > :commentCount
                        WHEN :commentCountOperator = '<' THEN COUNT(c.id) < :commentCount
                        WHEN :commentCountOperator = '=' THEN COUNT(c.id) = :commentCount
                        WHEN :commentCountOperator = '>=' THEN COUNT(c.id) >= :commentCount
                        WHEN :commentCountOperator = '<=' THEN COUNT(c.id) <= :commentCount
                        ELSE TRUE
                    END)
                ORDER BY p.posted_at DESC
                LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
            """,
      countQuery =
          """
                SELECT COUNT(DISTINCT p.id) FROM Posts p
                LEFT JOIN hashtag_details hd ON p.id = hd.post_id
                LEFT JOIN Hashtags h ON hd.hashtag_id = h.id
                WHERE (:captions IS NULL OR LOWER(CAST(p.captions AS TEXT)) LIKE LOWER(CONCAT('%', :captions, '%')))
                AND (:status IS NULL OR p.status = :status)
                AND (:audience IS NULL OR p.audience = :audience)
                AND (:isCommentVisible IS NULL OR p.is_comment_visible = CAST(:isCommentVisible AS boolean))
                AND (:isLikeVisible IS NULL OR p.is_like_visible = CAST(:isLikeVisible AS boolean))
                AND (:hashtag IS NULL OR LOWER(h.content) LIKE LOWER(CONCAT('%', :hashtag, '%')))
            """,
      nativeQuery = true)
  Page<Object[]> findPostsForTable(
      @Param("captions") String captions,
      @Param("status") Integer status,
      @Param("audience") String audience,
      @Param("isCommentVisible") Boolean isCommentVisible,
      @Param("isLikeVisible") Boolean isLikeVisible,
      @Param("hashtag") String hashtag,
      @Param("commentCount") Long commentCount,
      @Param("commentCountOperator") String commentCountOperator,
      Pageable pageable);
}
