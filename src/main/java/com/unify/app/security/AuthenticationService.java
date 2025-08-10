package com.unify.app.security;

import com.unify.app.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final TokenRepository tokenRepository;
  private final SecurityService securityService;

  public void saveUserToken(User user, String jti, String jwtToken) {
    Token token =
        Token.builder().user(user).jti(jti).token(jwtToken).expired(false).revoked(false).build();
    tokenRepository.save(token);
  }

  public void revokeAllUserTokens() {
    var validUserTokens =
        tokenRepository.findAllValidTokensByUser(securityService.getCurrentUserId());
    if (validUserTokens.isEmpty()) {
      return;
    }
    validUserTokens.forEach(
        t -> {
          t.setExpired(true);
          t.setRevoked(true);
        });
    tokenRepository.saveAll(validUserTokens);
  }

  public void processTokenCleanup() {
    var expiredTokens =
        tokenRepository.findAllInvalidTokensByUser(securityService.getCurrentUserId());
    if (!expiredTokens.isEmpty()) {
      tokenRepository.deleteAll(expiredTokens);
    }
  }
}
