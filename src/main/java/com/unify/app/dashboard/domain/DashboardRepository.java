package com.unify.app.dashboard.domain;

import com.unify.app.reports.domain.models.EntityType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardRepository {

  @PersistenceContext private EntityManager entityManager;

  // Get total users count
  public Long getTotalUsers() {
    Query query =
        entityManager.createQuery("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = 2");
    return (Long) query.getSingleResult();
  }

  // Get total users count for last month (a)
  public Long getTotalUsersLastMonth(
      LocalDateTime lastMonthStart, LocalDateTime currentMonthStart) {
    Query query =
        entityManager.createQuery(
            "SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = 2 AND u.createdAt >= :lastMonthStart AND u.createdAt < :currentMonthStart");
    query.setParameter("lastMonthStart", lastMonthStart);
    query.setParameter("currentMonthStart", currentMonthStart);
    return (Long) query.getSingleResult();
  }

  // Get new users count for current month (b)
  public Long getNewUsersThisMonth(LocalDateTime currentMonthStart) {
    Query query =
        entityManager.createQuery(
            "SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = 2 AND u.createdAt >= :currentMonthStart");
    query.setParameter("currentMonthStart", currentMonthStart);
    return (Long) query.getSingleResult();
  }

  // Get total posts count
  public Long getTotalPosts() {
    Query query = entityManager.createQuery("SELECT COUNT(p) FROM Post p WHERE p.status != 2");
    return (Long) query.getSingleResult();
  }

  // Get total posts count for last month (a)
  public Long getTotalPostsLastMonth(
      LocalDateTime lastMonthStart, LocalDateTime currentMonthStart) {
    Query query =
        entityManager.createQuery(
            "SELECT COUNT(p) FROM Post p WHERE p.status != 2 AND p.postedAt >= :lastMonthStart AND p.postedAt < :currentMonthStart");
    query.setParameter("lastMonthStart", lastMonthStart);
    query.setParameter("currentMonthStart", currentMonthStart);
    return (Long) query.getSingleResult();
  }

  // Get new posts count for current month (b)
  public Long getNewPostsThisMonth(LocalDateTime currentMonthStart) {
    Query query =
        entityManager.createQuery(
            "SELECT COUNT(p) FROM Post p WHERE p.status != 2 AND p.postedAt >= :currentMonthStart");
    query.setParameter("currentMonthStart", currentMonthStart);
    return (Long) query.getSingleResult();
  }

  // Get total pending reports count (status = 1)
  public Long getTotalPendingReports() {
    Query query = entityManager.createQuery("SELECT COUNT(r) FROM Report r WHERE r.status = 0");
    return (Long) query.getSingleResult();
  }

  // Get active users count (users who have posted in the last 30 days)
  public Long getActiveUsers(LocalDateTime thirtyDaysAgo) {
    Query query =
        entityManager.createQuery(
            "SELECT COUNT(DISTINCT p.user.id) FROM Post p WHERE p.status != 2 AND p.postedAt >= :thirtyDaysAgo");
    query.setParameter("thirtyDaysAgo", thirtyDaysAgo);
    return (Long) query.getSingleResult();
  }

  // Get active users count for last month (a)
  public Long getActiveUsersLastMonth(
      LocalDateTime lastMonthStart, LocalDateTime currentMonthStart) {
    Query query =
        entityManager.createQuery(
            "SELECT COUNT(DISTINCT p.user.id) FROM Post p WHERE p.status != 2 AND p.postedAt >= :lastMonthStart AND p.postedAt < :currentMonthStart");
    query.setParameter("lastMonthStart", lastMonthStart);
    query.setParameter("currentMonthStart", currentMonthStart);
    return (Long) query.getSingleResult();
  }

  // Get new active users count for current month (b)
  public Long getNewActiveUsersThisMonth(LocalDateTime currentMonthStart) {
    Query query =
        entityManager.createQuery(
            "SELECT COUNT(DISTINCT p.user.id) FROM Post p WHERE p.status != 2 AND p.postedAt >= :currentMonthStart");
    query.setParameter("currentMonthStart", currentMonthStart);
    return (Long) query.getSingleResult();
  }

  // Get new reports count for today
  public Long getNewReportsToday() {
    Query query =
        entityManager.createQuery(
            "SELECT COUNT(r) FROM Report r WHERE CAST(r.reportedAt AS date) = CURRENT_DATE");
    return (Long) query.getSingleResult();
  }

  // Get new users count by day for the last 7 days
  public List<Object[]> getNewUsersByDay(LocalDateTime startDate, LocalDateTime endDate) {
    Query query =
        entityManager.createQuery(
            "SELECT CAST(u.createdAt AS date) as date, COUNT(u) as count "
                + "FROM User u JOIN u.roles r "
                + "WHERE r.id = 2 AND u.createdAt >= :startDate AND u.createdAt < :endDate "
                + "GROUP BY CAST(u.createdAt AS date) "
                + "ORDER BY date");
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    return query.getResultList();
  }

  // Get active users count by day for the last 7 days
  public List<Object[]> getActiveUsersByDay(LocalDateTime startDate, LocalDateTime endDate) {
    Query query =
        entityManager.createQuery(
            "SELECT CAST(p.postedAt AS date) as date, COUNT(DISTINCT p.user.id) as count "
                + "FROM Post p "
                + "WHERE p.status != 2 AND p.postedAt >= :startDate AND p.postedAt < :endDate "
                + "GROUP BY CAST(p.postedAt AS date) "
                + "ORDER BY date");
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    return query.getResultList();
  }

  // Get new users count by week for the last 30 days
  public List<Object[]> getNewUsersByWeek(LocalDateTime startDate, LocalDateTime endDate) {
    Query query =
        entityManager.createQuery(
            "SELECT EXTRACT(WEEK FROM u.createdAt) as week, COUNT(u) as count "
                + "FROM User u JOIN u.roles r "
                + "WHERE r.id = 2 AND u.createdAt >= :startDate AND u.createdAt < :endDate "
                + "GROUP BY EXTRACT(WEEK FROM u.createdAt) "
                + "ORDER BY week");
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    return query.getResultList();
  }

  // Get active users count by week for the last 30 days
  public List<Object[]> getActiveUsersByWeek(LocalDateTime startDate, LocalDateTime endDate) {
    Query query =
        entityManager.createQuery(
            "SELECT EXTRACT(WEEK FROM p.postedAt) as week, COUNT(DISTINCT p.user.id) as count "
                + "FROM Post p "
                + "WHERE p.status != 2 AND p.postedAt >= :startDate AND p.postedAt < :endDate "
                + "GROUP BY EXTRACT(WEEK FROM p.postedAt) "
                + "ORDER BY week");
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    return query.getResultList();
  }

  // Get new users count by month for the last 12 months
  public List<Object[]> getNewUsersByMonth(LocalDateTime startDate, LocalDateTime endDate) {
    Query query =
        entityManager.createQuery(
            "SELECT EXTRACT(MONTH FROM u.createdAt) as month, COUNT(u) as count "
                + "FROM User u JOIN u.roles r "
                + "WHERE r.id = 2 AND u.createdAt >= :startDate AND u.createdAt < :endDate "
                + "GROUP BY EXTRACT(MONTH FROM u.createdAt) "
                + "ORDER BY month");
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    return query.getResultList();
  }

  // Get active users count by month for the last 12 months
  public List<Object[]> getActiveUsersByMonth(LocalDateTime startDate, LocalDateTime endDate) {
    Query query =
        entityManager.createQuery(
            "SELECT EXTRACT(MONTH FROM p.postedAt) as month, COUNT(DISTINCT p.user.id) as count "
                + "FROM Post p "
                + "WHERE p.status != 2 AND p.postedAt >= :startDate AND p.postedAt < :endDate "
                + "GROUP BY EXTRACT(MONTH FROM p.postedAt) "
                + "ORDER BY month");
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    return query.getResultList();
  }

  // Get reported posts with latest report info
  public List<Object[]> getReportedPosts() {
    Query query =
        entityManager.createQuery(
            "SELECT r.reportedId, MAX(r.reportedAt) as latestReportedAt, COUNT(r) as reportCount, "
                + "p.captions, CONCAT(u.firstName, ' ', u.lastName) as authorName, u.id as authorId "
                + "FROM Report r "
                + "LEFT JOIN Post p ON p.id = r.reportedId "
                + "LEFT JOIN User u ON u.id = p.user.id "
                + "WHERE r.entityType = 'POST' "
                + "AND r.status = 0 "
                + "GROUP BY r.reportedId, p.captions, u.firstName, u.lastName, u.id "
                + "ORDER BY latestReportedAt DESC");
    return query.getResultList();
  }

  // Get reported users with latest report info
  public List<Object[]> getReportedUsers() {
    Query query =
        entityManager.createQuery(
            "SELECT r.reportedId, MAX(r.reportedAt) as latestReportedAt, COUNT(r) as reportCount, "
                + "CONCAT(u.firstName, ' ', u.lastName) as userName, u.email as userEmail, "
                + "(SELECT a.url FROM Avatar a WHERE a.user.id = u.id ORDER BY a.createdAt DESC LIMIT 1) as userAvatar "
                + "FROM Report r "
                + "LEFT JOIN User u ON u.id = r.reportedId "
                + "WHERE r.entityType = 'USER' "
                + "AND r.status = 0 "
                + "GROUP BY r.reportedId, u.firstName, u.lastName, u.email, u.id "
                + "ORDER BY latestReportedAt DESC");
    return query.getResultList();
  }

  // Get reported comments with latest report info
  public List<Object[]> getReportedComments() {
    Query query =
        entityManager.createQuery(
            "SELECT r.reportedId, MAX(r.reportedAt) as latestReportedAt, COUNT(r) as reportCount, "
                + "c.content as commentContent, CONCAT(u.firstName, ' ', u.lastName) as authorName, u.id as authorId, "
                + "p.captions as postTitle "
                + "FROM Report r "
                + "LEFT JOIN Comment c ON c.id = r.reportedId "
                + "LEFT JOIN User u ON u.id = c.user.id "
                + "LEFT JOIN Post p ON p.id = c.post.id "
                + "WHERE r.entityType = 'COMMENT' "
                + "AND r.status = 0 "
                + "GROUP BY r.reportedId, c.content, u.firstName, u.lastName, u.id, p.captions "
                + "ORDER BY latestReportedAt DESC");
    return query.getResultList();
  }

  // Get pending reports count by type
  public List<Object[]> getPendingReportsByType() {
    Query query =
        entityManager.createQuery(
            "SELECT r.entityType, COUNT(r) as count "
                + "FROM Report r "
                + "WHERE r.status = 0 "
                + "GROUP BY r.entityType");
    return query.getResultList();
  }

  // Get resolved reports count for today (status changed from 1 to 0 or 2)
  public Long getResolvedReportsToday() {
    Query query =
        entityManager.createQuery(
            "SELECT COUNT(r) FROM Report r "
                + "WHERE CAST(r.reportedAt AS date) = CURRENT_DATE AND r.status IN (0, 2)");
    return (Long) query.getSingleResult();
  }

  // Get total reports count by entity type
  public Long getTotalReportsByType(EntityType entityType) {
    Query query =
        entityManager.createQuery("SELECT COUNT(r) FROM Report r WHERE r.entityType = :entityType");
    query.setParameter("entityType", entityType);
    return (Long) query.getSingleResult();
  }

  // Get pending reports count by entity type
  public Long getPendingReportsByType(EntityType entityType) {
    Query query =
        entityManager.createQuery(
            "SELECT COUNT(r) FROM Report r WHERE r.entityType = :entityType AND r.status = 1");
    query.setParameter("entityType", entityType);
    return (Long) query.getSingleResult();
  }
}
