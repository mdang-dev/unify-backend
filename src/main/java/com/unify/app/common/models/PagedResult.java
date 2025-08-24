package com.unify.app.common.models;

import java.util.List;
import org.springframework.data.domain.Page;

public record PagedResult<T>(
    List<T> data,
    long totalElements,
    int currentPageNo,
    int totalPages,
    boolean isFirstPage,
    boolean isLastPage,
    boolean hasNextPage,
    boolean hasPreviousPage) {

  public PagedResult(Page<T> page) {
    this(
        page.getContent(),
        page.getTotalElements(),
        page.getNumber(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast(),
        page.hasNext(),
        page.hasPrevious());
  }
}
