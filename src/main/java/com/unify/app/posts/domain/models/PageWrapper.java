package com.unify.app.posts.domain.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import org.springframework.data.domain.Page;

// Or create a simple page wrapper
@JsonPropertyOrder({
  "content",
  "pageNumber",
  "pageSize",
  "totalElements",
  "totalPages",
  "hasNext",
  "hasPrevious"
})
public class PageWrapper<T> {
  private final Page<T> page;

  public PageWrapper(Page<T> page) {
    this.page = page;
  }

  public List<T> getContent() {
    return page.getContent();
  }

  public int getPageNumber() {
    return page.getNumber();
  }

  public int getPageSize() {
    return page.getSize();
  }

  public long getTotalElements() {
    return page.getTotalElements();
  }

  public int getTotalPages() {
    return page.getTotalPages();
  }

  public boolean isHasNext() {
    return page.hasNext();
  }

  public boolean isHasPrevious() {
    return page.hasPrevious();
  }
}
