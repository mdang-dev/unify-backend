package com.unify.app.security;

import com.unify.app.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final TokenRepository tokenRepository;

  public void saveUserToken(User user, String jwtToken) {
    revokeAllUserTokens(user);
    Token token = Token.builder().user(user).token(jwtToken).expired(false).revoked(false).build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
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
}
