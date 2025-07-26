package com.unify.app.users.domain.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {
  boolean success;
  String message;

  public ApiResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }
}
