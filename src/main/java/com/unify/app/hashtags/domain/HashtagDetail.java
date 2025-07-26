package com.unify.app.hashtags.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unify.app.posts.domain.Post;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "hashtag_details")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class HashtagDetail implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  @ManyToOne
  @JoinColumn(name = "post_id", nullable = false)
  @JsonIgnore
  Post post;

  @ManyToOne
  @JoinColumn(name = "hashtag_id", nullable = false)
  @JsonIgnore
  Hashtag hashtag;
}
