package com.unify.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final CustomUserDetailsService customUserDetailsService;
  private final TokenRepository tokenRepository;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = jwtService.getTokenFromRequest(request);
    if (token == null || !StringUtils.hasText(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      var isTokenValid =
          tokenRepository
              .findByToken(token)
              .map(t -> !t.getExpired() && !t.getRevoked())
              .orElse(false);

      if (StringUtils.hasText(token) && jwtService.validToken(token) && isTokenValid) {

        String email = jwtService.extractUsername(token);
        if (email == null || email.isEmpty()) {
          throw new RuntimeException("Invalid token: unable to extract username");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        if (userDetails == null) {
          throw new RuntimeException("User not found for the given token");
        }

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    } catch (Exception e) {
      System.out.println("Error during JWT authentication: " + e.getMessage());
    }
    filterChain.doFilter(request, response);
  }
}
