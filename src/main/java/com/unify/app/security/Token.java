package com.unify.app.security;

import com.unify.app.users.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "tokens")
class Token implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  @Column(nullable = false)
  String jti;

  @Column(nullable = false)
  String token;

  @Column(nullable = false)
  Boolean expired;

  @Column(nullable = false)
  Boolean revoked;

  @ManyToOne
  @JoinColumn(name = "user_id")
  User user;
}
