package com.unify.app.media.domain;

import com.unify.app.common.exceptions.ResourceNotFoundException;

public class StreamNotFoundException extends ResourceNotFoundException {
  StreamNotFoundException(String message) {
    super(message);
  }
}
