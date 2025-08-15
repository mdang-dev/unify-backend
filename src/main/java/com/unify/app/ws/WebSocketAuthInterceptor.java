package com.unify.app.ws;

import com.unify.app.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JwtService jwt;
  private final UserDetailsService users;
  private final WebSocketPerformanceMonitor performanceMonitor;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    long startTime = System.currentTimeMillis();
    try {
      var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
      if (accessor == null) {
        return message;
      }

      if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
        performanceMonitor.incrementMessagesReceived();
        return message;
      }

      var token = accessor.getFirstNativeHeader("token");
      if (token == null || token.trim().isEmpty()) {
        log.warn("No token found in WebSocket connection");
        return message;
      }

      String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

      if (!jwt.validToken(cleanToken)) {
        log.warn("Invalid token in WebSocket connection");
        return message;
      }

      String username = jwt.extractUsername(cleanToken);
      if (username == null || username.trim().isEmpty()) {
        log.warn("Could not extract username from token");
        return message;
      }

      UserDetails user = users.loadUserByUsername(username);
      if (user == null) {
        log.warn("User not found for username: {}", username);
        return message;
      }

      accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

      performanceMonitor.incrementActiveConnections();
      performanceMonitor.addConnectionTime(System.currentTimeMillis() - startTime);

      return message;

    } catch (Exception e) {
      log.error("Error during WebSocket authentication: {}", e.getMessage());
      return message;
    }
  }

  @Override
  public void afterSendCompletion(
      Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
    if (ex != null) {
      log.error("Error sending WebSocket message: {}", ex.getMessage());
      performanceMonitor.incrementMessagesSent();
    } else if (sent) {
      performanceMonitor.incrementMessagesSent();
    }
  }
}
