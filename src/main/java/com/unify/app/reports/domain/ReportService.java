package com.unify.app.reports.domain;

import com.unify.app.comments.domain.Comment;
import com.unify.app.comments.domain.CommentMapper;
import com.unify.app.comments.domain.CommentService;
import com.unify.app.notifications.domain.NotificationService;
import com.unify.app.notifications.domain.ReportNotificationService;
import com.unify.app.posts.domain.Post;
import com.unify.app.posts.domain.PostMapper;
import com.unify.app.posts.domain.PostService;
import com.unify.app.reports.domain.models.AggregatedReportDto;
import com.unify.app.reports.domain.models.EntityType;
import com.unify.app.reports.domain.models.ReportDto;
import com.unify.app.reports.domain.models.ReportSummaryDto;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.UserMapper;
import com.unify.app.users.domain.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;
  private final PostService postService;
  private final CommentService commentService;
  private final UserService userService;
  private final ReportMapper reportMapper;
  private final UserMapper userMapper;
  private final PostMapper postMapper;
  private final CommentMapper commentMapper;
  private final ReportImageRepository reportImageRepository;
  private final NotificationService notificationService;
  private final ReportNotificationService reportNotificationService;

  public static final int PENDING = 0;
  public static final int APPROVED = 1;
  public static final int REJECTED = 2;
  public static final int RESOLVED = 3;
  public static final int CANCELED = 4;

  /** Retrieves a report with detailed reported entity. */
  public ReportDto getDetailedReportById(String reportId) {
    Report report =
        reportRepository
            .findById(reportId)
            .orElseThrow(
                () -> new IllegalArgumentException("Report not found with ID: " + reportId));
    return toDetailedReportDto(report);
  }

  public ReportDto findById(String id) {
    return reportRepository
        .findById(id)
        .map(reportMapper::toReportDTO)
        .orElseThrow(() -> new ReportException("Report not found with id + " + id));
  }

  /** Returns detailed report with reporters as List<UserDto>. */
  public com.unify.app.reports.domain.models.ReportWithReportersDto findDetailedById(String id) {
    Report report =
        reportRepository
            .findById(id)
            .orElseThrow(() -> new ReportException("Report not found with id + " + id));

    ReportDto base = reportMapper.toReportDTO(report);
    var extended = new com.unify.app.reports.domain.models.ReportWithReportersDto();
    extended.setId(base.getId());
    extended.setReportedId(base.getReportedId());
    extended.setReportedAt(base.getReportedAt());
    extended.setEntityType(base.getEntityType());
    extended.setStatus(base.getStatus());
    extended.setReason(base.getReason());
    extended.setAdminReason(base.getAdminReason());
    extended.setReportedEntity(base.getReportedEntity());
    extended.setImages(base.getImages());

    // Load reporter user IDs by reported target (no brittle column positions)
    List<String> reporterIds =
        reportRepository.findReporterUserIdsForTarget(
            report.getReportedId(), report.getEntityType().name());
    var reporterDtos =
        reporterIds.stream()
            .map(userService::findUserById)
            .map(userMapper::toUserDTO)
            .collect(java.util.stream.Collectors.toList());
    extended.setReporters(reporterDtos);

    return extended;
  }

  /**
   * Fetch all reports for a given reportedId and return enriched results (same shape as detailed
   * response, per report).
   */
  public List<com.unify.app.reports.domain.models.ReportWithReportersDto> findDetailedByReportedId(
      String reportedId) {
    List<Report> reports = reportRepository.findByReportedId(reportedId);
    if (reports == null || reports.isEmpty()) {
      throw new ReportException("No reports found for reportedId: " + reportedId);
    }

    // Determine target type from first report
    String entityType = reports.get(0).getEntityType().name();

    // Load reporter user IDs for this target and map to UserDto
    List<String> reporterIds =
        reportRepository.findReporterUserIdsForTarget(reportedId, entityType);
    var reporterDtos =
        reporterIds.stream()
            .map(userService::findUserById)
            .map(userMapper::toUserDTO)
            .collect(Collectors.toList());

    // Build enriched DTOs for each report
    return reports.stream()
        .map(
            report -> {
              ReportDto base = reportMapper.toReportDTO(report);
              var dto = new com.unify.app.reports.domain.models.ReportWithReportersDto();
              dto.setId(base.getId());
              dto.setReportedId(base.getReportedId());
              dto.setReportedAt(base.getReportedAt());
              dto.setEntityType(base.getEntityType());
              dto.setStatus(base.getStatus());
              dto.setReason(base.getReason());
              dto.setAdminReason(base.getAdminReason());
              dto.setReportedEntity(
                  getReportedEntity(report.getReportedId(), report.getEntityType()));
              dto.setImages(base.getImages());
              dto.setReporters(reporterDtos);
              return dto;
            })
        .collect(Collectors.toList());
  }

  // Safely convert JDBC array or collection to List<String>
  private List<String> extractStringList(Object value) {
    try {
      if (value == null) return List.of();
      if (value instanceof java.sql.Array sqlArray) {
        Object arr = sqlArray.getArray();
        if (arr instanceof String[] sarr) return java.util.Arrays.asList(sarr);
        Object[] anyArr = (Object[]) arr;
        List<String> out = new java.util.ArrayList<>(anyArr.length);
        for (Object o : anyArr) if (o != null) out.add(o.toString());
        return out;
      }
      if (value instanceof List<?> list) {
        List<String> out = new java.util.ArrayList<>(list.size());
        for (Object o : list) if (o != null) out.add(o.toString());
        return out;
      }
      if (value.getClass().isArray()) {
        Object[] anyArr = (Object[]) value;
        List<String> out = new java.util.ArrayList<>(anyArr.length);
        for (Object o : anyArr) if (o != null) out.add(o.toString());
        return out;
      }
      return List.of(value.toString());
    } catch (Exception e) {
      return List.of();
    }
  }

  /** Creates a report for a post. */
  public ReportDto createPostReport(String reportedId, String reason, List<String> urls) {
    return createReport(reportedId, reason, EntityType.POST, urls);
  }

  /** Creates a report for a user. */
  public ReportDto createUserReport(String reportedId, String reason, List<String> urls) {
    return createReport(reportedId, reason, EntityType.USER, urls);
  }

  /** Creates a report for a comment. */
  public ReportDto createCommentReport(String reportedId, String reason, List<String> urls) {
    return createReport(reportedId, reason, EntityType.COMMENT, urls);
  }

  /** Creates a report with validation. */
  //  public ReportDto createReport(String reportedId, String reason, EntityType entityType) {
  //    String userId = userService.getMyInfo().id();
  //
  //    if (reason == null || reason.trim().isEmpty()) {
  //      throw new IllegalArgumentException("Report reason cannot be empty.");
  //    }
  //
  //    if (isSelfReport(userId, reportedId, entityType)) {
  //      throw new IllegalArgumentException("You cannot report your own content.");
  //    }
  //
  //    if (reportRepository.existsByUserIdAndReportedIdAndEntityType(userId, reportedId,
  // entityType)) {
  //      throw new ReportException("You have already reported this content.");
  //    }
  //
  //    User user = userService.findUserById(userId);
  //
  //    Report report = new Report();
  //    report.setUser(user);
  //    report.setEntityType(entityType);
  //    report.setReportedId(reportedId);
  //    report.setReason(reason);
  //    report.setStatus(PENDING);
  //    report.setReportedAt(LocalDateTime.now());
  //
  //    return reportMapper.toReportDTO(reportRepository.save(report));
  //  }
  public ReportDto createReport(
      String reportedId, String reason, EntityType entityType, List<String> urls) {
    String userId = userService.getMyInfo().id();

    if (reason == null || reason.trim().isEmpty()) {
      throw new IllegalArgumentException("Report reason cannot be empty.");
    }

    if (isSelfReport(userId, reportedId, entityType)) {
      throw new IllegalArgumentException("You cannot report your own content.");
    }

    if (reportRepository.existsByUserIdAndReportedIdAndEntityType(userId, reportedId, entityType)) {
      throw new ReportException("You have already reported this content.");
    }

    User user = userService.findUserById(userId);

    Report report = new Report();
    report.setUser(user);
    report.setEntityType(entityType);
    report.setReportedId(reportedId);
    report.setReason(reason);
    report.setStatus(PENDING);
    report.setReportedAt(LocalDateTime.now());

    if (urls != null && !urls.isEmpty()) {
      for (String imageUrl : urls) {
        ReportImage img = new ReportImage();
        img.setUrl(imageUrl);
        img.setReport(report); // Gán chiều ManyToOne
        report.getImages().add(img); // Gán chiều OneToMany
      }
    }

    reportRepository.save(report);

    return reportMapper.toReportDTO(report);
  }

  /** Determines if a user is reporting their own content. */
  private boolean isSelfReport(String userId, String reportedId, EntityType type) {
    return switch (type) {
      case POST -> postService
          .findByOptionalPostId(reportedId)
          .map(Post::getUser)
          .map(User::getId)
          .filter(userId::equals)
          .isPresent();
      case COMMENT -> commentService
          .findOptionalById(reportedId)
          .map(Comment::getUser)
          .map(User::getId)
          .filter(userId::equals)
          .isPresent();
      case USER -> userId.equals(reportedId);
    };
  }

  /** Returns a report with full reported entity data. */
  private ReportDto toDetailedReportDto(Report report) {
    ReportDto dto = reportMapper.toReportDTO(report);
    dto.setReportedEntity(getReportedEntity(report.getReportedId(), report.getEntityType()));
    return dto;
  }

  /** Returns the reported object by type. */
  private Object getReportedEntity(String reportedId, EntityType type) {
    return switch (type) {
      case POST -> postMapper.toPostDto(postService.findById(reportedId));

      case USER -> userMapper.toUserDTO(userService.findUserById(reportedId));

      case COMMENT -> commentMapper.toCommentDTO(commentService.findById(reportedId));
    };
  }

  /** Updates the status of a report. */
  // public ReportDto updateReportStatus(String reportId, int status, String
  // adminReason) {
  // if (status < PENDING || status > CANCELED) {
  // throw new ReportException("Invalid status value: " + status);
  // }
  //
  // Report report =
  // reportRepository
  // .findById(reportId)
  // .orElseThrow(() -> new ReportException("Report not found."));
  //
  // report.setStatus(status);
  // report.setAdminReason(adminReason);
  //
  // if (status == APPROVED) {
  // handleApprovalAction(report);
  // }
  //
  // return reportMapper.toReportDTO(reportRepository.save(report));
  // }
  //
  // private void handleApprovalAction(Report report) {
  // switch (report.getEntityType()) {
  // case POST -> {
  // Post post = postService.findById(report.getReportedId());
  // post.setStatus(2); // Hide post
  // postService.update(post);
  // }
  // case USER -> {
  // User user = userService.findUserById(report.getReportedId());
  // user.setReportApprovalCount(user.getReportApprovalCount() + 1);
  // int count = user.getReportApprovalCount();
  // if (count >= 5) {
  // user.setStatus(2); // Permanent ban
  // } else if (count >= 3 && user.getStatus() != 2) {
  // user.setStatus(1); // Temporary ban
  // }
  // userService.update(user);
  // }
  // case COMMENT -> {
  // Comment comment = commentService.findById(report.getReportedId());
  // comment.setStatus(2); // Hide comment
  // commentService.update(comment);
  // commentService
  // .findByParentId(comment.getId())
  // .forEach(
  // reply -> {
  // reply.setStatus(2);
  // commentService.update(reply);
  // });
  // }
  // }
  // }
  public ReportDto updateReportStatus(String reportId, int status, String adminReason) {
    if (status < PENDING || status > CANCELED) {
      throw new ReportException("Invalid status value: " + status);
    }

    Report report =
        reportRepository
            .findById(reportId)
            .orElseThrow(() -> new ReportException("Report not found."));

    report.setStatus(status);
    report.setAdminReason(adminReason);

    if (status == APPROVED) {
      handleApprovalAction(report);
    } else if (status == REJECTED) {
      ReportDto reportDto = reportMapper.toReportDTO(report);
      handleRejectionAction(reportDto);
    }

    return reportMapper.toReportDTO(reportRepository.save(report));
  }

  private void handleApprovalAction(Report report) {
    switch (report.getEntityType()) {
      case POST -> {
        Post post = postService.findById(report.getReportedId());
        post.setStatus(2); // Hide post
        postService.update(post);

        // Send notification to the post owner about the approved report
        reportNotificationService.sendReportApprovedNotification(
            post.getUser().getId(), // Post owner ID
            report.getEntityType(),
            report.getReportedId(),
            "SYSTEM", // System admin ID
            report.getAdminReason() // Admin reason
            );
      }
      case USER -> {
        User user = userService.findUserById(report.getReportedId());
        user.setReportApprovalCount(user.getReportApprovalCount() + 1);
        int count = user.getReportApprovalCount();

        // Send type-specific notification using ReportNotificationService
        reportNotificationService.sendReportApprovedNotification(
            user.getId(),
            report.getEntityType(),
            report.getReportedId(),
            "SYSTEM", // System admin ID
            report.getAdminReason() // Admin reason
            );

        // Check thresholds and apply appropriate actions
        if (count >= 5) {
          user.setStatus(2); // Permanent ban
          // Send permanent ban notification
          reportNotificationService.sendAccountBannedNotification(user.getId(), count, "SYSTEM");
        } else if (count >= 3 && user.getStatus() != 2) {
          user.setStatus(1); // Temporary ban
          // Send temporary ban notification
          reportNotificationService.sendAccountSuspendedNotification(user.getId(), count, "SYSTEM");
        }

        userService.update(user);

        // Send real-time WebSocket event for report count update
        notificationService.sendReportCountUpdate(user.getId(), count);
      }
      case COMMENT -> {
        Comment comment = commentService.findById(report.getReportedId());
        comment.setStatus(2); // Hide comment
        commentService.update(comment);

        // Send notification to the comment owner about the approved report
        reportNotificationService.sendReportApprovedNotification(
            comment.getUser().getId(), // Comment owner ID
            report.getEntityType(),
            report.getReportedId(),
            "SYSTEM", // System admin ID
            report.getAdminReason() // Admin reason
            );

        // Hide all replies to this comment
        commentService
            .findByParentId(comment.getId())
            .forEach(
                reply -> {
                  reply.setStatus(2);
                  commentService.update(reply);
                });
      }
    }

    reportRepository
        .findByReportedIdAndEntityType(report.getReportedId(), report.getEntityType())
        .forEach(
            r -> {
              r.setStatus(APPROVED);
              reportRepository.save(r);
            });
  }

  private void handleRejectionAction(ReportDto reportDto) {
    // Update all reports for the same target to REJECTED status for consistency
    reportRepository
        .findByReportedIdAndEntityType(reportDto.getReportedId(), reportDto.getEntityType())
        .forEach(
            r -> {
              r.setStatus(REJECTED);
              r.setAdminReason(
                  reportDto.getAdminReason()); // Use the same admin reason for consistency
              reportRepository.save(r);
            });
  }

  /** Admin-only delete for a report. */
  @PreAuthorize("hasRole('ADMIN')")
  public void removeReport(String reportId) {
    Report report =
        reportRepository
            .findById(reportId)
            .orElseThrow(() -> new ReportException("Report not found."));
    reportRepository.delete(report);
  }

  /** Get reports filed by a specific user. */
  public List<ReportDto> getDetailedReportsByUsername(String username) {
    User user = userService.findUserByUsername(username);
    List<Report> reports = reportRepository.findByUserIdOrderByReportedAtDesc(user.getId());

    if (reports.isEmpty()) {
      throw new IllegalArgumentException("No reports found for this user.");
    }

    return reports.stream().map(this::toDetailedReportDto).collect(Collectors.toList());
  }

  /** Get reports on posts owned by a specific user. */
  public List<ReportDto> getReportsOfUserPosts(String username) {
    User user = userService.findUserByUsername(username);

    List<Report> reports =
        reportRepository.findReportsOfPostsOwnedByUser(EntityType.POST, user.getId());
    return reports.stream().map(this::toDetailedReportDto).collect(Collectors.toList());
  }

  /** Retrieve reports filtered by status and entity type. */
  public List<ReportDto> getReportsByStatuses(List<Integer> statuses, EntityType type) {
    validateStatuses(statuses);

    return reportRepository.findByStatusInAndEntityType(statuses, type).stream()
        .map(
            report -> {
              try {
                return toDetailedReportDto(report);
              } catch (Exception e) {
                System.out.println(
                    "⚠️ Lỗi khi map reportId=" + report.getId() + ": " + e.getMessage());
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public List<ReportDto> findByEntityType(EntityType type) {
    return reportRepository.findByEntityType(type).stream()
        .map(reportMapper::toReportDTO)
        .collect(Collectors.toList());
  }

  public List<ReportDto> findByStatusAndEntityType(Integer status, EntityType type) {
    return reportRepository.findByStatusAndEntityType(status, EntityType.POST).stream()
        .map(reportMapper::toReportDTO)
        .collect(Collectors.toList());
  }

  private void validateStatuses(List<Integer> statuses) {
    for (int status : statuses) {
      if (status < PENDING || status > CANCELED) {
        throw new ReportException("Invalid report status: " + status);
      }
    }
  }

  public Page<ReportSummaryDto> findDistinctReportedPosts(
      Integer status, LocalDateTime reportedAtFrom, LocalDateTime reportedAtTo, Pageable pageable) {

    return reportRepository.findDistinctReportedPosts(
        status, reportedAtFrom, reportedAtTo, pageable);
  }

  /**
   * Get distinct reported users with aggregated report information
   *
   * @param status Filter by report status (optional)
   * @param reportedAtFrom Filter by reportedAt start date (optional)
   * @param reportedAtTo Filter by reportedAt end date (optional)
   * @param pageable Pagination and sorting parameters
   * @return Page of ReportSummaryDto for users
   */
  public Page<ReportSummaryDto> findDistinctReportedUsers(
      Integer status, LocalDateTime reportedAtFrom, LocalDateTime reportedAtTo, Pageable pageable) {

    return reportRepository.findDistinctReportedUsers(
        status, reportedAtFrom, reportedAtTo, pageable);
  }

  public List<AggregatedReportDto> getAggregatedReports(List<Integer> statuses, EntityType type) {
    // Lấy danh sách nhóm báo cáo theo reportedId và entityType
    List<AggregatedReportProjection> rawGroups =
        reportRepository.findGroupedReports(statuses, type);

    return rawGroups.stream()
        .map(
            group -> {
              // Lấy tất cả các report tương ứng với cùng reportedId & entityType
              List<Report> reports =
                  reportRepository.findByReportedIdAndEntityType(
                      group.getReportedId(), group.getEntityType());

              // Lấy danh sách lý do không trùng lặp
              List<String> reasons =
                  reports.stream()
                      .map(Report::getReason)
                      .filter(Objects::nonNull)
                      .distinct()
                      .collect(Collectors.toList());

              // Lấy toàn bộ ảnh liên quan từ reportImageRepository
              List<String> urls =
                  reports.stream()
                      .flatMap(
                          (Report report) -> {
                            List<ReportImage> images =
                                reportImageRepository.findByReportId(report.getId());
                            return images != null ? images.stream() : Stream.<ReportImage>empty();
                          })
                      .map(ReportImage::getUrl)
                      .collect(Collectors.toList());

              // Trả về đối tượng tổng hợp
              return AggregatedReportDto.builder()
                  .reportedId(group.getReportedId())
                  .entityType(group.getEntityType())
                  .reportCount(group.getReportCount().intValue())
                  .status(group.getMaxStatus())
                  .reasons(reasons)
                  .urls(urls)
                  .build();
            })
        .collect(Collectors.toList());
  }
}
