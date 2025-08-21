package com.unify.app.dashboard.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
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
    Query query = entityManager.createQuery("SELECT COUNT(r) FROM Report r WHERE r.status = 1");
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
}
