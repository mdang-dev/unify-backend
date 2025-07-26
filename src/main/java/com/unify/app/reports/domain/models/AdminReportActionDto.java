package com.unify.app.reports.domain.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AdminReportActionDto(@Min(0) Integer status, @NotBlank String adminReason) {}
