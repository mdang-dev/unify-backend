package com.unify.app.hashtags.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface HashtagDetailRepository extends JpaRepository<HashtagDetail, String> {
  @Query("SELECT hd.post.id FROM HashtagDetail hd WHERE hd.hashtag.id = ?1")
  List<String> findPostByHashtagId(String hashtagId);
}
