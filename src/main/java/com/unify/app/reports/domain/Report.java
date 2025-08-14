package com.unify.app.reports.domain;

import com.unify.app.reports.domain.models.EntityType;
import com.unify.app.users.domain.User;
import jakarta.persistence.CascadeType;
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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "reports")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Report implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @Column(name = "reported_id", nullable = false)
  String reportedId;

  @Column(name = "reported_at", nullable = false)
  LocalDateTime reportedAt;

  @Column(name = "entity_type", nullable = false)
  @Enumerated(EnumType.STRING)
  EntityType entityType;

  Integer status;

  @Column(name = "reason", nullable = false)
  String reason;

  @Column(name = "admin_reason")
  String adminReason;

  @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReportImage> images = new ArrayList<>();
}
