package com.unify.app.users.domain;

import com.unify.app.security.AuthenticationService;
import com.unify.app.security.JwtService;
import com.unify.app.users.domain.models.TokenResponse;
import com.unify.app.users.domain.models.auth.UserLoginCmd;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final AuthenticationService authenticationService;
  private final PasswordEncoder passwordEncoder;
  private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();

  public TokenResponse login(UserLoginCmd cmd) {
    Authentication authentication;
    User user = userService.findByEmail(cmd.email());

    if (cmd.email().equals(user.getEmail())
        && isMatchesPassword(user.getPassword(), cmd.password())) {
      authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(cmd.email(), cmd.password()));
      SecurityContextHolder.getContext().setAuthentication(authentication);

      String tokenGenerated = jwtService.generateToken(cmd.email());
      authenticationService.saveUserToken(user, tokenGenerated);
      return new TokenResponse(tokenGenerated);
    }
    return null;
  }

  public void incrementFailedAttempts(String email) {
    int attempts = attemptsCache.getOrDefault(email, 0);
    attemptsCache.put(email, attempts + 1);
  }

  public int getFailedAttempts(String email) {
    return attemptsCache.getOrDefault(email, 0);
  }

  public void clearFailedAttempts(String email) {
    attemptsCache.remove(email);
  }

  private boolean isMatchesPassword(String storePassword, String password) {
    return passwordEncoder.matches(password, storePassword);
  }
}
