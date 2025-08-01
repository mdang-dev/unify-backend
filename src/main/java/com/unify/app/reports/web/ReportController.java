package com.unify.app.reports.web;

import com.unify.app.reports.domain.ReportService;
import com.unify.app.reports.domain.models.AdminReportActionDto;
import com.unify.app.reports.domain.models.EntityType;
import com.unify.app.reports.domain.models.ReportDto;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @GetMapping("/reportUser/status")
  public ResponseEntity<?> findFilteredReportsByStatusesAndType(
      @RequestParam List<Integer> statuses, @RequestParam EntityType entityType) {

    List<ReportDto> reports = reportService.getReportsByStatuses(statuses, entityType);
    return ResponseEntity.ok(reports);
  }

  @GetMapping("/reportUser/user-reports")
  public ResponseEntity<?> findReportsByUsername(@RequestParam String username) {

    List<ReportDto> reports = reportService.getDetailedReportsByUsername(username);
    return ResponseEntity.ok(reports);
  }

  @GetMapping("/reportUser/reported-my-posts")
  public ResponseEntity<?> findReportsOfMyPosts(@RequestParam String username) {
    try {
      List<ReportDto> reports = reportService.getReportsOfUserPosts(username);
      return ResponseEntity.ok(reports);
    } catch (UsernameNotFoundException | IllegalArgumentException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  @GetMapping("/allPosts")
  public List<ReportDto> findAllReportedPosts() {
    return reportService.findByEntityType(EntityType.POST);
  }

  @GetMapping("/allComments")
  public List<ReportDto> findAllReportedComments() {
    List<Integer> allStatuses = List.of(0, 1, 2, 3, 4);
    return reportService.getReportsByStatuses(allStatuses, EntityType.COMMENT);
  }

  @GetMapping("/filter/{status}")
  public List<ReportDto> findFilteredReportedPosts(@PathVariable Integer status) {
    return reportService.findByStatusAndEntityType(status, EntityType.POST);
  }

  @GetMapping("/filterComments/{status}")
  public List<ReportDto> findFilteredReportedComments(@PathVariable Integer status) {
    return reportService.findByStatusAndEntityType(status, EntityType.COMMENT);
  }

  @GetMapping("/{id}")
  public ReportDto getReport(@PathVariable String id) {
    return reportService.findById(id);
  }

  @PostMapping("/post")
  public ResponseEntity<ReportDto> createPostReport(
      @RequestParam String reportedId, @RequestParam String reason) {
    ReportDto reportDTO = reportService.createPostReport(reportedId, reason);
    return ResponseEntity.status(HttpStatus.CREATED).body(reportDTO);
  }

  @PostMapping("/user")
  public ResponseEntity<ReportDto> createUserReport(
      @RequestParam String reportedId, @RequestParam String reason) {
    ReportDto reportDTO = reportService.createUserReport(reportedId, reason);
    return ResponseEntity.status(HttpStatus.CREATED).body(reportDTO);
  }

  @PostMapping("/comment")
  public ResponseEntity<ReportDto> createCommentReport(
      @RequestParam String reportedId, @RequestParam String reason) {
    ReportDto reportDTO = reportService.createCommentReport(reportedId, reason);
    return ResponseEntity.status(HttpStatus.CREATED).body(reportDTO);
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<ReportDto> updateReportStatus(
      @PathVariable String id, @Valid @RequestBody AdminReportActionDto actionDTO) {
    ReportDto updatedReport =
        reportService.updateReportStatus(id, actionDTO.status(), actionDTO.adminReason());
    return ResponseEntity.ok(updatedReport);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> removeReport(@PathVariable String id) {
    reportService.removeReport(id);
    return ResponseEntity.ok("Remove Report Successfully!");
  }
}
