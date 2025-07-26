package com.unify.app.posts.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface MediaRepository extends JpaRepository<Media, String> {
  @Query("FROM Media o WHERE o.post.id = ?1")
  List<Media> findByPostId(String postId);
}
