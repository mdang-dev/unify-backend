package com.unify.app.posts.domain;

import com.unify.app.common.exceptions.ResourceNotFoundException;

public class PostNotFoundException extends ResourceNotFoundException {
  public PostNotFoundException(String message) {
    super(message);
  }
}
