package com.unify.app.followers.domain;

import com.unify.app.followers.domain.models.FollowerUserId;
import com.unify.app.users.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "followers")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Follower implements Serializable {

  @EmbeddedId FollowerUserId id;

  @ManyToOne
  @JoinColumn(
      name = "follower_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false,
      nullable = false)
  User userFollower;

  @ManyToOne
  @JoinColumn(
      name = "following_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false,
      nullable = false)
  User userFollowing;

  @Column(name = "create_at", nullable = false)
  @CreationTimestamp
  LocalDateTime createAt;
}
