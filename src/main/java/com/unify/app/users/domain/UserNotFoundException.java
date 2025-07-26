package com.unify.app.users.domain;

import com.unify.app.common.exceptions.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
  public UserNotFoundException(String message) {
    super(message);
  }
}
