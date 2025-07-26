package com.unify.app.hashtags.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface HashtagRepository extends JpaRepository<Hashtag, String> {

  Optional<Hashtag> findByContent(String content);
}
