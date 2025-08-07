package com.unify.app.users.domain;

import com.unify.app.media.domain.Stream;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
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
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  @Column(name = "first_name", nullable = false)
  String firstName;

  @Column(name = "last_name", nullable = false)
  String lastName;

  @Column(name = "user_name", nullable = false, unique = true)
  String username;

  @Column(nullable = false)
  String phone;

  @Column(nullable = false, unique = true)
  String email;

  @Column(nullable = false)
  String password;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  LocalDateTime updatedAt;

  @Column(nullable = false)
  Boolean gender;

  @Column(nullable = false)
  LocalDate birthDay;

  String location;
  String education;

  // status = 1; Khóa tạm thời
  // status = 2; Khóa vĩnh viễn
  // status = 0; Bình thường
  @Column(nullable = false)
  Integer status;

  @Column(name = "report_approval_count")
  @Builder.Default
  Integer reportApprovalCount = 0;

  @Column(name = "work_at")
  String workAt;

  @Column(name = "biography")
  String biography;

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
  Set<Role> roles;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "user")
  Set<Avatar> avatars;

  @OneToOne(mappedBy = "user")
  Stream stream;

  public Avatar latestAvatar() {
    if (avatars == null || avatars.isEmpty()) {
      return null;
    }

    // Find the avatar with the latest createdAt timestamp
    return avatars.stream()
        .max(
            (a1, a2) -> {
              if (a1.getCreatedAt() == null && a2.getCreatedAt() == null) return 0;
              if (a1.getCreatedAt() == null) return -1;
              if (a2.getCreatedAt() == null) return 1;
              return a1.getCreatedAt().compareTo(a2.getCreatedAt());
            })
        .orElse(null);
  }
}
