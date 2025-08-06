package com.unify.app.users.domain.models;

import java.util.List;

public record UserPagedResponse(
    List<UserDto> users,
    int currentPage,
    int totalPages,
    long totalElements,
    int pageSize,
    boolean hasNext,
    boolean hasPrevious) {}
