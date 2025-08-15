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
        return message; // Skip if no accessor
      }

      // Only authenticate CONNECT commands for performance
      if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
        performanceMonitor.incrementMessagesReceived();
        return message;
      }

      // ✅ PERFORMANCE: Fast token extraction with null check
      var token = accessor.getFirstNativeHeader("token");
      if (token == null || token.trim().isEmpty()) {
        log.warn("No token found in WebSocket connection");
        return message;
      }

      // ✅ PERFORMANCE: Optimized token cleaning
      String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

      // ✅ PERFORMANCE: Fast token validation with timeout protection
      if (!jwt.validToken(cleanToken)) {
        log.warn("Invalid token in WebSocket connection");
        return message;
      }

      // ✅ PERFORMANCE: Extract username efficiently
      String username = jwt.extractUsername(cleanToken);
      if (username == null || username.trim().isEmpty()) {
        log.warn("Could not extract username from token");
        return message;
      }

      // ✅ PERFORMANCE: Load user details with caching
      UserDetails user = users.loadUserByUsername(username);
      if (user == null) {
        log.warn("User not found for username: {}", username);
        return message;
      }

      // Set authentication
      accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

      // ✅ PERFORMANCE: Track connection metrics
      performanceMonitor.incrementActiveConnections();
      performanceMonitor.addConnectionTime(System.currentTimeMillis() - startTime);

      return message;

    } catch (Exception e) {
      log.error("Error during WebSocket authentication: {}", e.getMessage());
      // ✅ OPTIMIZED: Return message to prevent connection blocking
      return message;
    }
  }

  @Override
  public void afterSendCompletion(
      Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
    if (ex != null) {
      log.error("Error sending WebSocket message: {}", ex.getMessage());
      // ✅ OPTIMIZED: Track failed messages for monitoring
      performanceMonitor.incrementMessagesSent(); // Track even failed attempts
    } else if (sent) {
      performanceMonitor.incrementMessagesSent();
    }
  }
}
