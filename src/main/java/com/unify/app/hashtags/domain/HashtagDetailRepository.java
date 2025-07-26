package com.unify.app.hashtags.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface HashtagDetailRepository extends JpaRepository<HashtagDetail, String> {
  @Query("SELECT o.post.id FROM HashtagDetail o WHERE o.hashtag.id = ?1")
  List<String> findPostByHashtagId(String hashtagId);
}
