package com.unify.app.reports.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportImageRepository extends JpaRepository<ReportImage, String> {
  List<ReportImage> findByReportId(String reportId);
}
