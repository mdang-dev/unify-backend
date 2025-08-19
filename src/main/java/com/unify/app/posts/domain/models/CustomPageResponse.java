package com.unify.app.posts.domain.models;

import java.util.List;

// Custom response class to avoid Spring PageImpl serialization issues
public class CustomPageResponse<T> {
  private final List<T> content;
  private final int pageNumber;
  private final int pageSize;
  private final long totalElements;
  private final int totalPages;
  private final boolean hasNext;
  private final boolean hasPrevious;

  public CustomPageResponse(
      List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages) {
    this.content = content;
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
    this.hasNext = pageNumber < totalPages - 1;
    this.hasPrevious = pageNumber > 0;
  }

  // Getters
  public List<T> getContent() {
    return content;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public boolean isHasNext() {
    return hasNext;
  }

  public boolean isHasPrevious() {
    return hasPrevious;
  }

  public boolean isEmpty() {
    return content.isEmpty();
  }

  public int getNumberOfElements() {
    return content.size();
  }
}
