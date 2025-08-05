package com.unify.app.posts.saved;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SavedPostRepository extends JpaRepository<SavedPost, String> {

  // Find all saved posts by a specific user
  List<SavedPost> findByUserId(String userId);

  // Find all saved posts by user, ordered by the time they were saved
  // (descending)
  List<SavedPost> findByUserIdOrderBySavedAtDesc(String userId);

  @Query(
      "SELECT sp FROM SavedPost sp WHERE sp.user.id = :userId AND sp.post.audience = 'PUBLIC' AND sp.post.status = 1 ORDER BY sp.savedAt DESC")
  List<SavedPost> findPublicSavedPostsByUserIdOrderBySavedAtDesc(@Param("userId") String userId);

  // Find a saved post record by user ID and post ID
  Optional<SavedPost> findByUserIdAndPostId(String userId, String postId);

  // Check if a post has been saved by the user
  boolean existsByUserIdAndPostId(String userId, String postId);

  // Delete a saved post record by user ID and post ID
  void deleteByUserIdAndPostId(String userId, String postId);

  // Find all saved post records for a specific post ID
  List<SavedPost> findByPostId(String postId);
}
