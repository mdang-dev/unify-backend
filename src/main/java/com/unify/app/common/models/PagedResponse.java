package com.unify.app.common.models;

import java.util.List;

/**
 * A stable DTO for paginated responses that avoids Spring Data PageImpl serialization issues. This
 * provides a consistent JSON structure for paginated data.
 */
public record PagedResponse<T>(
    List<T> content, // The data content
    int page, // Current page number (0-based)
    int size, // Page size
    long totalElements, // Total number of elements
    int totalPages, // Total number of pages
    boolean first, // Is this the first page?
    boolean last, // Is this the last page?
    boolean empty // Is the page empty?
    ) {

  /** Creates a PagedResponse from Spring Data Page object */
  public static <T> PagedResponse<T> from(org.springframework.data.domain.Page<T> page) {
    return new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast(),
        page.isEmpty());
  }
}
