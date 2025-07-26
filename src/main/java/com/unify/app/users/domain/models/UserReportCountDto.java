package com.unify.app.users.domain.models;

public record UserReportCountDto(
    String id, String username, String email, Long reportApprovalCount) {}
