package com.unify.app.posts.domain;

import com.unify.app.common.exceptions.ResourceNotFoundException;

public class MediaNotFoundException extends ResourceNotFoundException {
  public MediaNotFoundException(String message) {
    super(message);
  }
}
