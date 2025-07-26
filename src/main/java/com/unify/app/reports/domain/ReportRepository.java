package com.unify.app.reports.domain;

import com.unify.app.reports.domain.models.EntityType;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface ReportRepository extends JpaRepository<Report, String> {

  List<Report> findByStatusIn(List<Integer> statuses);

  List<Report> findByReportedId(String reportedId);

  Optional<Report> findById(String id);

  List<Report> findByEntityType(EntityType entityType);

  boolean existsByUserIdAndReportedIdAndEntityType(
      String userId, String reportedId, EntityType entityType);

  List<Report> findByStatusAndEntityType(Integer status, EntityType entityType);

  List<Report> findByStatusInAndEntityType(List<Integer> statuses, EntityType entityType);

  // Shortcut for finding reports with entityType = COMMENT
  default List<Report> findByEntityTypeComment() {
    return findByEntityType(EntityType.COMMENT);
  }

  List<Report> findByUserIdAndEntityType(String userId, EntityType entityType);

  List<Report> findByReportedIdAndEntityTypeAndStatusIn(
      String reportedId, EntityType entityType, List<Integer> statuses);

  List<Report> findByUserId(String userId);

  // Get reports where the logged-in user's posts have been reported by others
  @Query(
      "SELECT r FROM Report r "
          + "JOIN Post p ON r.reportedId = p.id "
          + "WHERE r.entityType = :entityType AND p.user.id = :userId")
  List<Report> findReportsOfPostsOwnedByUser(
      @Param("entityType") EntityType entityType, @Param("userId") String userId);
}
