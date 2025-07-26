package com.unify.app.reports.domain;

import com.unify.app.comments.domain.Comment;
import com.unify.app.comments.domain.CommentMapper;
import com.unify.app.comments.domain.CommentService;
import com.unify.app.posts.domain.Post;
import com.unify.app.posts.domain.PostMapper;
import com.unify.app.posts.domain.PostService;
import com.unify.app.reports.domain.models.EntityType;
import com.unify.app.reports.domain.models.ReportDto;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.UserMapper;
import com.unify.app.users.domain.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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

  /** Creates a report for a post. */
  public ReportDto createPostReport(String reportedId, String reason) {
    return createReport(reportedId, reason, EntityType.POST);
  }

  /** Creates a report for a user. */
  public ReportDto createUserReport(String reportedId, String reason) {
    return createReport(reportedId, reason, EntityType.USER);
  }

  public ReportDto findById(String id) {
    return reportRepository
        .findById(id)
        .map(reportMapper::toReportDTO)
        .orElseThrow(() -> new ReportException("Report not found with id + " + id));
  }

  /** Creates a report for a comment. */
  public ReportDto createCommentReport(String reportedId, String reason) {
    return createReport(reportedId, reason, EntityType.COMMENT);
  }

  /** Creates a report with validation. */
  public ReportDto createReport(String reportedId, String reason, EntityType entityType) {
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

    return reportMapper.toReportDTO(reportRepository.save(report));
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

      case COMMENT -> commentMapper.toCommentDto(commentService.findById(reportedId));
    };
  }

  /** Updates the status of a report. */
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
    }

    return reportMapper.toReportDTO(reportRepository.save(report));
  }

  private void handleApprovalAction(Report report) {
    switch (report.getEntityType()) {
      case POST -> {
        Post post = postService.findById(report.getReportedId());
        post.setStatus(2); // Hide post
        postService.update(post);
      }
      case USER -> {
        User user = userService.findUserById(report.getReportedId());
        user.setReportApprovalCount(user.getReportApprovalCount() + 1);
        int count = user.getReportApprovalCount();
        if (count >= 5) {
          user.setStatus(2); // Permanent ban
        } else if (count >= 3 && user.getStatus() != 2) {
          user.setStatus(1); // Temporary ban
        }
        userService.update(user);
      }
      case COMMENT -> {
        Comment comment = commentService.findById(report.getReportedId());
        comment.setStatus(2); // Hide comment
        commentService.update(comment);
        commentService
            .findByParentId(comment.getId())
            .forEach(
                reply -> {
                  reply.setStatus(2);
                  commentService.update(reply);
                });
      }
    }
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
    List<Report> reports = reportRepository.findByUserId(user.getId());

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
        .map(this::toDetailedReportDto)
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
}
