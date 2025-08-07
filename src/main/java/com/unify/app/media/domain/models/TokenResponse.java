package com.unify.app.media.domain.models;

public record TokenResponse(String token, String error) {
  public static TokenResponse success(String token) {
    return new TokenResponse(token, null);
  }

  public static TokenResponse error(String error) {
    return new TokenResponse(null, error);
  }

  public boolean isSuccess() {
    return token != null && error == null;
  }
}
