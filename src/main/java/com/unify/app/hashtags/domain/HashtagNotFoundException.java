package com.unify.app.hashtags.domain;

import com.unify.app.common.exceptions.ResourceNotFoundException;

public class HashtagNotFoundException extends ResourceNotFoundException {

  HashtagNotFoundException(String message) {
    super(message);
  }
}
