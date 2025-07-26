package com.unify.app.comments.domain;

import com.unify.app.common.exceptions.ResourceNotFoundException;

public class CommentNotFoundException extends ResourceNotFoundException {
  public CommentNotFoundException(String message) {
    super(message);
  }
}
