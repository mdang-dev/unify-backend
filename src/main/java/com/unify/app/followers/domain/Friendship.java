package com.unify.app.followers.domain;

import com.unify.app.followers.domain.models.FriendshipStatus;
import com.unify.app.followers.domain.models.FriendshipUserId;
import com.unify.app.users.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "Friendships")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship implements Serializable {

  @EmbeddedId FriendshipUserId id;

  @ManyToOne
  @JoinColumn(
      name = "friendship_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false,
      nullable = false)
  User friend;

  @ManyToOne
  @JoinColumn(
      name = "user_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false,
      nullable = false)
  User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "friendship_status", nullable = false)
  FriendshipStatus friendshipStatus;

  @Column(name = "create_at", nullable = false)
  @CreationTimestamp
  LocalDateTime createAt;

  @Column(name = "update_at")
  @UpdateTimestamp
  LocalDateTime updateAt;
}
