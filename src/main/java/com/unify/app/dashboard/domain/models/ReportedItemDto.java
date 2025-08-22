package com.unify.app.dashboard.domain.models;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportedItemDto {
  private String reportedId;
  private LocalDateTime latestReportedAt;
  private Long reportCount;
  private String type;

  // For posts
  private String postTitle;
  private String authorName;
  private String authorId;

  // For users
  private String userName;
  private String userEmail;
  private String userAvatar;

  // For comments
  private String commentContent;
  private String parentPostTitle;
}
