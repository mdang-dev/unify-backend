package com.unify.app.ws;

import com.unify.app.security.JwtService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

  private final JwtService jwt;
  private final UserDetailsService users;
  private final WebSocketPerformanceMonitor performanceMonitor;

  // ✅ NEW: Constructor for dependency injection
  public WebSocketAuthInterceptor(
      JwtService jwt, UserDetailsService users, WebSocketPerformanceMonitor performanceMonitor) {
    this.jwt = jwt;
    this.users = users;
    this.performanceMonitor = performanceMonitor;
  }

  // ✅ NEW: Rate limiting for WebSocket connections
  private final Map<String, AtomicInteger> connectionAttempts = new ConcurrentHashMap<>();
  private final Map<String, Long> lastAttemptTime = new ConcurrentHashMap<>();
  private static final int MAX_ATTEMPTS_PER_MINUTE = 10;
  private static final long RATE_LIMIT_WINDOW_MS = 60000; // 1 minute

  // ✅ NEW: Rate limiting check
  private boolean isRateLimited(String clientIp) {
    long now = System.currentTimeMillis();
    long oneMinuteAgo = now - RATE_LIMIT_WINDOW_MS;

    // Clean old attempts
    lastAttemptTime.entrySet().removeIf(entry -> entry.getValue() < oneMinuteAgo);

    AtomicInteger attempts =
        connectionAttempts.computeIfAbsent(clientIp, k -> new AtomicInteger(0));
    long lastAttempt = lastAttemptTime.getOrDefault(clientIp, 0L);

    if (now - lastAttempt > RATE_LIMIT_WINDOW_MS) {
      attempts.set(0);
    }

    if (attempts.incrementAndGet() > MAX_ATTEMPTS_PER_MINUTE) {
      // Only log rate limit violations
      log.warn(
          "Rate limit exceeded for IP: {} ({} attempts in 1 minute)", clientIp, attempts.get());
      return true;
    }

    lastAttemptTime.put(clientIp, now);
    return false;
  }

  // ✅ NEW: Extract client IP from message
  private String getClientIp(Message<?> message) {
    try {
      // Try to get IP from headers
      String ip = message.getHeaders().get("X-Forwarded-For", String.class);
      if (ip != null && !ip.trim().isEmpty()) {
        return ip.split(",")[0].trim(); // Get first IP if multiple
      }

      // Fallback to remote address
      ip = message.getHeaders().get("X-Real-IP", String.class);
      if (ip != null && !ip.trim().isEmpty()) {
        return ip.trim();
      }

      // Default fallback
      return "unknown";
    } catch (Exception e) {
      // Only log critical IP extraction errors
      log.error("Failed to extract client IP: {}", e.getMessage());
      return "unknown";
    }
  }

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

      // ✅ NEW: Rate limiting check for CONNECT commands
      String clientIp = getClientIp(message);
      if (isRateLimited(clientIp)) {
        // Only log rate limit rejections
        log.warn("Connection rejected due to rate limiting for IP: {}", clientIp);
        return null; // Reject connection
      }

      var token = accessor.getFirstNativeHeader("token");
      if (token == null || token.trim().isEmpty()) {
        // Only log missing token attempts
        log.warn("No token found in WebSocket connection from IP: {}", clientIp);
        return message;
      }

      String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

      if (!jwt.validToken(cleanToken)) {
        // Only log invalid token attempts
        log.warn("Invalid token in WebSocket connection from IP: {}", clientIp);
        performanceMonitor.incrementAuthenticationFailures();
        return message;
      }

      String username = jwt.extractUsername(cleanToken);
      if (username == null || username.trim().isEmpty()) {
        // Only log token extraction failures
        log.warn("Could not extract username from token for IP: {}", clientIp);
        performanceMonitor.incrementAuthenticationFailures();
        return message;
      }

      UserDetails user = users.loadUserByUsername(username);
      if (user == null) {
        // Only log user not found
        log.warn("User not found for username: {} from IP: {}", username, clientIp);
        performanceMonitor.incrementAuthenticationFailures();
        return message;
      }

      accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

      performanceMonitor.incrementActiveConnections();
      performanceMonitor.addConnectionTime(System.currentTimeMillis() - startTime);

      // Remove successful connection log - not needed in production
      return message;

    } catch (Exception e) {
      // Only log critical authentication errors
      log.error("Error during WebSocket authentication: {}", e.getMessage());
      performanceMonitor.incrementConnectionErrors();
      return message;
    }
  }

  @Override
  public void afterSendCompletion(
      Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
    if (ex != null) {
      // Only log critical send errors
      log.error("Error sending WebSocket message: {}", ex.getMessage());
      performanceMonitor.incrementMessagesSent();
    } else if (sent) {
      performanceMonitor.incrementMessagesSent();
    }
  }
}
