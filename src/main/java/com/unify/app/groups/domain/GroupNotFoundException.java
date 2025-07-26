package com.unify.app.groups.domain;

import com.unify.app.common.exceptions.ResourceNotFoundException;

public class GroupNotFoundException extends ResourceNotFoundException {
  GroupNotFoundException(String message) {
    super(message);
  }

  public static GroupNotFoundException forNotFound() {
    return new GroupNotFoundException("Group not found !");
  }
}
