package com.unify.app.dashboard.domain;

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
}
