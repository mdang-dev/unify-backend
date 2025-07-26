package com.unify.app.users.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "avatars")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avatar implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  @Column(nullable = false)
  String url;

  @Column(name = "created_at", nullable = false)
  @CreationTimestamp
  LocalDateTime createdAt;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  User user;
}
