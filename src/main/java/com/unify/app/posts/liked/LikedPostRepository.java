package com.unify.app.posts.liked;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface LikedPostRepository extends JpaRepository<LikedPost, String> {
  @Query("SELECT l FROM LikedPost l WHERE l.user.id = :userId AND l.post.id = :postId")
  LikedPost findByUserIdAndPostId(@Param("userId") String userId, @Param("postId") String postId);

  @Modifying
  @Transactional
  @Query("DELETE FROM LikedPost l WHERE l.user.id = :userId AND l.post.id = :postId")
  int deleteByUserIdAndPostId(@Param("userId") String userId, @Param("postId") String postId);

  boolean existsByUserIdAndPostId(String userId, String postId);

  @Query("SELECT COUNT(l) FROM LikedPost l WHERE l.post.id = :postId")
  int countByPostId(@Param("postId") String postId);

  @Query("SELECT lp FROM LikedPost lp WHERE lp.user.id = :userId")
  List<LikedPost> findAllByUserId(@Param("userId") String userId);
}
