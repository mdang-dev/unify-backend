package com.unify.app.media.domain;

import com.unify.app.users.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(
    name = "streams",
    indexes = {
      @Index(name = "idx_name", columnList = "name"),
      @Index(name = "idx_ingress_id", columnList = "ingress_id")
    })
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stream implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  @Column(unique = true)
  String roomId;

  @Column(nullable = false, unique = true)
  String name;

  String title;

  String description;

  @Column(name = "thumbnail_url")
  String thumbnailUrl;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @Column(name = "ingress_id", unique = true)
  String ingressId;

  @Column(name = "server_url")
  String serverUrl;

  @Column(name = "stream_key")
  String streamKey;

  @Builder.Default Boolean isLive = false;

  @Builder.Default Boolean isChatEnabled = false;

  @Builder.Default Boolean isChatDelayed = false;

  @Builder.Default Boolean isChatFollowersOnly = false;

  @CreationTimestamp LocalDateTime createAt;

  @UpdateTimestamp LocalDateTime updateAt;

  LocalDateTime startTime;
  LocalDateTime endTime;
}
