package com.unify.app.reports.domain;

import com.unify.app.reports.domain.models.EntityType;
import com.unify.app.reports.domain.models.ReportSummaryDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, String> {

  List<Report> findByStatusIn(List<Integer> statuses);

  List<Report> findByReportedId(String reportedId);

  Optional<Report> findById(String id);

  // Fetch a report by id and include the list of reporters (user usernames) who submitted
  @Query(
      value =
          """
          SELECT r.id,
                 r.reported_id,
                 r.reported_at,
                 r.entity_type,
                 r.status,
                 r.reason,
                 r.admin_reason,
                 r.user_id,
                 (
                   SELECT ARRAY_REMOVE(ARRAY_AGG(DISTINCT u2.id), NULL)
                   FROM reports r2
                   LEFT JOIN users u2 ON u2.id = r2.user_id
                   WHERE r2.reported_id = r.reported_id AND r2.entity_type = r.entity_type
                 ) AS reporters
          FROM reports r
          WHERE r.id = :reportId
          """,
      nativeQuery = true)
  Object[] findReportWithReportersById(@Param("reportId") String reportId);

  // Fetch distinct reporter user IDs for a given target (reportedId + entityType)
  @Query(
      value =
          """
          SELECT DISTINCT u.id
          FROM reports r
          JOIN users u ON u.id = r.user_id
          WHERE r.reported_id = :reportedId AND r.entity_type = :entityType
          """,
      nativeQuery = true)
  List<String> findReporterUserIdsForTarget(
      @Param("reportedId") String reportedId, @Param("entityType") String entityType);

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

  List<Report> findByReportedIdAndEntityType(String reportedId, EntityType entityType);

  List<Report> findByReportedIdAndEntityTypeAndStatusIn(
      String reportedId, EntityType entityType, List<Integer> statuses);

  List<Report> findByUserIdOrderByReportedAtDesc(String userId);

  List<Report> findByUserId(String userId);

  // Get reports where the logged-in user's posts have been reported by others
  @Query(
      "SELECT r FROM Report r "
          + "JOIN Post p ON r.reportedId = p.id "
          + "WHERE r.entityType = :entityType AND r.status = 1 AND p.user.id = :userId ORDER BY r.reportedAt DESC")
  List<Report> findReportsOfPostsOwnedByUser(
      @Param("entityType") EntityType entityType, @Param("userId") String userId);

  // Updated upstream
  // Get distinct reported posts with aggregated data - wrapper method
  default Page<ReportSummaryDto> findDistinctReportedPosts(
      Integer status, LocalDateTime from, LocalDateTime to, Pageable pageable) {

    boolean hasStatus = status != null;
    boolean hasFrom = from != null;
    boolean hasTo = to != null;

    if (hasStatus && hasFrom && hasTo) {
      return findDistinctReportedPostsWithAllFilters(status, from, to, pageable);
    } else if (hasStatus && hasFrom) {
      return findDistinctReportedPostsWithStatusAndFrom(status, from, pageable);
    } else if (hasStatus && hasTo) {
      return findDistinctReportedPostsWithStatusAndTo(status, to, pageable);
    } else if (hasFrom && hasTo) {
      return findDistinctReportedPostsWithDateRange(from, to, pageable);
    } else if (hasStatus) {
      return findDistinctReportedPostsWithStatus(status, pageable);
    } else if (hasFrom) {
      return findDistinctReportedPostsWithFrom(from, pageable);
    } else if (hasTo) {
      return findDistinctReportedPostsWithTo(to, pageable);
    } else {
      return findDistinctReportedPostsWithNoFilters(pageable);
    }
  }

  // Get distinct reported posts with aggregated data - all filters
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.POST,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.POST
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      SUBSTRING(p.captions, 1, 50)
    )
    FROM Report r
    LEFT JOIN Post p ON p.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.POST
      AND r.status = :status
      AND r.reportedAt >= :from
      AND r.reportedAt <= :to
    GROUP BY r.reportedId, p.captions
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedPostsWithAllFilters(
      @Param("status") Integer status,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      Pageable pageable);

  // Get distinct reported posts with aggregated data - status and from date
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.POST,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.POST
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      SUBSTRING(p.captions, 1, 50)
    )
    FROM Report r
    LEFT JOIN Post p ON p.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.POST
      AND r.status = :status
      AND r.reportedAt >= :from
    GROUP BY r.reportedId, p.captions
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedPostsWithStatusAndFrom(
      @Param("status") Integer status, @Param("from") LocalDateTime from, Pageable pageable);

  // Get distinct reported posts with aggregated data - status and to date
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.POST,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.POST
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      SUBSTRING(p.captions, 1, 50)
    )
    FROM Report r
    LEFT JOIN Post p ON p.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.POST
      AND r.status = :status
      AND r.reportedAt <= :to
    GROUP BY r.reportedId, p.captions
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedPostsWithStatusAndTo(
      @Param("status") Integer status, @Param("to") LocalDateTime to, Pageable pageable);

  // Get distinct reported posts with aggregated data - date range only
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.POST,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.POST
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      SUBSTRING(p.captions, 1, 50)
    )
    FROM Report r
    LEFT JOIN Post p ON p.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.POST
      AND r.reportedAt >= :from
      AND r.reportedAt <= :to
    GROUP BY r.reportedId, p.captions
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedPostsWithDateRange(
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

  // Get distinct reported posts with aggregated data - status only
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.POST,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.POST
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      SUBSTRING(p.captions, 1, 50)
    )
    FROM Report r
    LEFT JOIN Post p ON p.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.POST
      AND r.status = :status
    GROUP BY r.reportedId, p.captions
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedPostsWithStatus(
      @Param("status") Integer status, Pageable pageable);

  // Get distinct reported posts with aggregated data - from date only
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.POST,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.POST
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      SUBSTRING(p.captions, 1, 50)
    )
    FROM Report r
    LEFT JOIN Post p ON p.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.POST
      AND r.reportedAt >= :from
    GROUP BY r.reportedId, p.captions
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedPostsWithFrom(
      @Param("from") LocalDateTime from, Pageable pageable);

  // Get distinct reported posts with aggregated data - to date only
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.POST,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.POST
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      SUBSTRING(p.captions, 1, 50)
    )
    FROM Report r
    LEFT JOIN Post p ON p.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.POST
      AND r.reportedAt <= :to
    GROUP BY r.reportedId, p.captions
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedPostsWithTo(
      @Param("to") LocalDateTime to, Pageable pageable);

  // Get distinct reported posts with aggregated data - no filters
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.POST,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.POST
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      SUBSTRING(p.captions, 1, 50)
    )
    FROM Report r
    LEFT JOIN Post p ON p.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.POST
    GROUP BY r.reportedId, p.captions
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedPostsWithNoFilters(Pageable pageable);

  // Get distinct reported users with aggregated data - all filters
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.USER,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.USER
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      COALESCE(
        CASE WHEN u.firstName IS NOT NULL AND u.lastName IS NOT NULL
             THEN CONCAT(u.firstName, ' ', u.lastName)
             ELSE NULL END,
        CONCAT('@', u.username)
      )
    )
    FROM Report r
    LEFT JOIN User u ON u.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.USER
      AND r.status = :status
      AND r.reportedAt >= :from
      AND r.reportedAt <= :to
    GROUP BY r.reportedId, u.firstName, u.lastName, u.username
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedUsersWithAllFilters(
      @Param("status") Integer status,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      Pageable pageable);

  // Get distinct reported users with aggregated data - status and from date
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.USER,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.USER
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      COALESCE(
        CASE WHEN u.firstName IS NOT NULL AND u.lastName IS NOT NULL
             THEN CONCAT(u.firstName, ' ', u.lastName)
             ELSE NULL END,
        CONCAT('@', u.username)
      )
    )
    FROM Report r
    LEFT JOIN User u ON u.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.USER
      AND r.status = :status
      AND r.reportedAt >= :from
    GROUP BY r.reportedId, u.firstName, u.lastName, u.username
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedUsersWithStatusAndFrom(
      @Param("status") Integer status, @Param("from") LocalDateTime from, Pageable pageable);

  // Get distinct reported users with aggregated data - status and to date
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.USER,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.USER
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      COALESCE(
        CASE WHEN u.firstName IS NOT NULL AND u.lastName IS NOT NULL
             THEN CONCAT(u.firstName, ' ', u.lastName)
             ELSE NULL END,
        CONCAT('@', u.username)
      )
    )
    FROM Report r
    LEFT JOIN User u ON u.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.USER
      AND r.status = :status
      AND r.reportedAt <= :to
    GROUP BY r.reportedId, u.firstName, u.lastName, u.username
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedUsersWithStatusAndTo(
      @Param("status") Integer status, @Param("to") LocalDateTime to, Pageable pageable);

  // Get distinct reported users with aggregated data - date range only
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.USER,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.USER
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      COALESCE(
        CASE WHEN u.firstName IS NOT NULL AND u.lastName IS NOT NULL
             THEN CONCAT(u.firstName, ' ', u.lastName)
             ELSE NULL END,
        CONCAT('@', u.username)
      )
    )
    FROM Report r
    LEFT JOIN User u ON u.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.USER
      AND r.reportedAt >= :from
      AND r.reportedAt <= :to
    GROUP BY r.reportedId, u.firstName, u.lastName, u.username
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedUsersWithDateRange(
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

  // Get distinct reported users with aggregated data - status only
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.USER,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.USER
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      COALESCE(
        CASE WHEN u.firstName IS NOT NULL AND u.lastName IS NOT NULL
             THEN CONCAT(u.firstName, ' ', u.lastName)
             ELSE NULL END,
        CONCAT('@', u.username)
      )
    )
    FROM Report r
    LEFT JOIN User u ON u.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.USER
      AND r.status = :status
    GROUP BY r.reportedId, u.firstName, u.lastName, u.username
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedUsersWithStatus(
      @Param("status") Integer status, Pageable pageable);

  // Get distinct reported users with aggregated data - from date only
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.USER,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.USER
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      COALESCE(
        CASE WHEN u.firstName IS NOT NULL AND u.lastName IS NOT NULL
             THEN CONCAT(u.firstName, ' ', u.lastName)
             ELSE NULL END,
        CONCAT('@', u.username)
      )
    )
    FROM Report r
    LEFT JOIN User u ON u.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.USER
      AND r.reportedAt >= :from
    GROUP BY r.reportedId, u.firstName, u.lastName, u.username
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedUsersWithFrom(
      @Param("from") LocalDateTime from, Pageable pageable);

  // Get distinct reported users with aggregated data - to date only
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.USER,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.USER
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      COALESCE(
        CASE WHEN u.firstName IS NOT NULL AND u.lastName IS NOT NULL
             THEN CONCAT(u.firstName, ' ', u.lastName)
             ELSE NULL END,
        CONCAT('@', u.username)
      )
    )
    FROM Report r
    LEFT JOIN User u ON u.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.USER
      AND r.reportedAt <= :to
    GROUP BY r.reportedId, u.firstName, u.lastName, u.username
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedUsersWithTo(
      @Param("to") LocalDateTime to, Pageable pageable);

  // Get distinct reported users with aggregated data - no filters
  @Query(
      """
    SELECT new com.unify.app.reports.domain.models.ReportSummaryDto(
      r.reportedId,
      com.unify.app.reports.domain.models.EntityType.USER,
      COUNT(r.id),
      MAX(r.reportedAt),
      (SELECT r2.status FROM Report r2
         WHERE r2.reportedId = r.reportedId
           AND r2.entityType = com.unify.app.reports.domain.models.EntityType.USER
         ORDER BY r2.reportedAt DESC
         LIMIT 1
      ),
      COALESCE(
        CASE WHEN u.firstName IS NOT NULL AND u.lastName IS NOT NULL
             THEN CONCAT(u.firstName, ' ', u.lastName)
             ELSE NULL END,
        CONCAT('@', u.username)
      )
    )
    FROM Report r
    LEFT JOIN User u ON u.id = r.reportedId
    WHERE r.entityType = com.unify.app.reports.domain.models.EntityType.USER
    GROUP BY r.reportedId, u.firstName, u.lastName, u.username
    ORDER BY MAX(r.reportedAt) DESC
    """)
  Page<ReportSummaryDto> findDistinctReportedUsersWithNoFilters(Pageable pageable);

  // Wrapper method to route to appropriate query based on parameters
  default Page<ReportSummaryDto> findDistinctReportedUsers(
      Integer status, LocalDateTime from, LocalDateTime to, Pageable pageable) {

    boolean hasStatus = status != null;
    boolean hasFrom = from != null;
    boolean hasTo = to != null;

    if (hasStatus && hasFrom && hasTo) {
      return findDistinctReportedUsersWithAllFilters(status, from, to, pageable);
    } else if (hasStatus && hasFrom) {
      return findDistinctReportedUsersWithStatusAndFrom(status, from, pageable);
    } else if (hasStatus && hasTo) {
      return findDistinctReportedUsersWithStatusAndTo(status, to, pageable);
    } else if (hasFrom && hasTo) {
      return findDistinctReportedUsersWithDateRange(from, to, pageable);
    } else if (hasStatus) {
      return findDistinctReportedUsersWithStatus(status, pageable);
    } else if (hasFrom) {
      return findDistinctReportedUsersWithFrom(from, pageable);
    } else if (hasTo) {
      return findDistinctReportedUsersWithTo(to, pageable);
    } else {
      return findDistinctReportedUsersWithNoFilters(pageable);
    }
  }

  // Repository method to fetch grouped data
  @Query(
      """
			 SELECT r.reportedId AS reportedId,
			        r.entityType AS entityType,
			        COUNT(r.id) AS reportCount,
			        MAX(r.status) AS maxStatus
			 FROM Report r
			 WHERE r.status IN :statuses AND r.entityType = :entityType
			 GROUP BY r.reportedId, r.entityType
			""")
  List<AggregatedReportProjection> findGroupedReports(
      List<Integer> statuses, EntityType entityType);
}
