package com.unify.app.posts.domain;

import com.unify.app.comments.domain.Comment;
import com.unify.app.posts.domain.models.Audience;
import com.unify.app.posts.liked.LikedPost;
import com.unify.app.users.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "Posts")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  String captions;

  // 0 -> hidden
  // 1 -> visible
  // 2 -> sensitive/ violent content/ delete
  @Default Integer status = 1;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Default
  Audience audience = Audience.PUBLIC;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @Column(name = "posted_at", nullable = false, updatable = false)
  @CreationTimestamp
  LocalDateTime postedAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  LocalDateTime updatedAt;

  @Column(name = "is_comment_visible", nullable = false)
  @Default
  Boolean isCommentVisible = false;

  @Column(name = "is_like_visible", nullable = false)
  @Default
  Boolean isLikeVisible = false;

  @OneToMany(mappedBy = "post", orphanRemoval = true)
  Set<Media> media;

  @OneToMany(mappedBy = "post", orphanRemoval = true)
  Set<LikedPost> likedPosts;

  @OneToMany(mappedBy = "post", orphanRemoval = true)
  Set<Comment> comments;

  @Override
  public String toString() {
    return "Post [id="
        + id
        + ", captions="
        + captions
        + ", status="
        + status
        + ", audience="
        + audience
        + ", postedAt="
        + postedAt
        + ", updatedAt="
        + updatedAt
        + ", isCommentVisible="
        + isCommentVisible
        + ", isLikeVisible="
        + isLikeVisible
        + "]";
  }
}
