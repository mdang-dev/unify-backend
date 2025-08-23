package com.unify.app.hashtags.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface HashtagRepository extends JpaRepository<Hashtag, String> {

  Optional<Hashtag> findByContent(String content);

  @Query("SELECT hd.post.id FROM HashtagDetail hd WHERE hd.hashtag.id = ?1")
  List<String> findPostByHashtagId(String hashtagId);
}
