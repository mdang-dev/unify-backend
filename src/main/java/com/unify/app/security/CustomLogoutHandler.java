package com.unify.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

  private final TokenRepository tokenRepository;

  @Override
  public void logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
      response.setStatus(401);
      return;
    }
    String token = authHeader.substring(7);
    var storedToken = tokenRepository.findByToken(token).orElse(null);
    if (storedToken != null) {
      storedToken.setExpired(true);
      storedToken.setRevoked(true);
      tokenRepository.save(storedToken);
    }
  }
}
