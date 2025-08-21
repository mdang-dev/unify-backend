// package com.unify.app.ws;
//
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.atomic.AtomicInteger;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
// import org.springframework.stereotype.Component;
// import org.springframework.web.socket.messaging.SessionConnectedEvent;
// import org.springframework.web.socket.messaging.SessionDisconnectEvent;
// import org.springframework.web.socket.messaging.SessionSubscribeEvent;
// import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
//
// @Slf4j
// @Component
// public class WebSocketConnectionManager {
//
//  private final Map<String, UserConnectionInfo> userConnections = new ConcurrentHashMap<>();
//  private final AtomicInteger totalConnections = new AtomicInteger(0);
//
//  private static final int MAX_TOTAL_CONNECTIONS = 1000;
//  private static final int MAX_CONNECTIONS_PER_USER = 5;
//  private static final long CONNECTION_TIMEOUT_MS = 600000;
//
//  public static class UserConnectionInfo {
//    private final String userId;
//    private final long connectedAt;
//    private final String sessionId;
//    private int subscriptionCount;
//
//    public UserConnectionInfo(String userId, String sessionId) {
//      this.userId = userId;
//      this.sessionId = sessionId;
//      this.connectedAt = System.currentTimeMillis();
//      this.subscriptionCount = 0;
//    }
//
//    public boolean isExpired() {
//      return System.currentTimeMillis() - connectedAt > CONNECTION_TIMEOUT_MS;
//    }
//
//    public void incrementSubscriptions() {
//      subscriptionCount++;
//    }
//
//    public void decrementSubscriptions() {
//      if (subscriptionCount > 0) {
//        subscriptionCount--;
//      }
//    }
//
//    public String getUserId() {
//      return userId;
//    }
//
//    public long getConnectedAt() {
//      return connectedAt;
//    }
//
//    public String getSessionId() {
//      return sessionId;
//    }
//
//    public int getSubscriptionCount() {
//      return subscriptionCount;
//    }
//  }
//
//  public boolean canAcceptConnection(String userId, String sessionId) {
//    if (totalConnections.get() >= MAX_TOTAL_CONNECTIONS) {
//      log.warn("Total connection limit reached: {}", MAX_TOTAL_CONNECTIONS);
//      return false;
//    }
//
//    UserConnectionInfo existingInfo = userConnections.get(userId);
//    if (existingInfo != null) {
//      if (existingInfo.getSubscriptionCount() >= MAX_CONNECTIONS_PER_USER) {
//        log.warn("User {} connection limit reached: {}", userId, MAX_CONNECTIONS_PER_USER);
//        return false;
//      }
//    }
//
//    return true;
//  }
//
//  public void onSessionConnected(SessionConnectedEvent event) {
//    try {
//      StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//      String sessionId = accessor.getSessionId();
//      String userId = accessor.getUser() != null ? accessor.getUser().getName() : "anonymous";
//
//      if (canAcceptConnection(userId, sessionId)) {
//        UserConnectionInfo info = new UserConnectionInfo(userId, sessionId);
//        userConnections.put(userId, info);
//        totalConnections.incrementAndGet();
//      } else {
//        log.warn("Connection rejected for user: {}, session: {}", userId, sessionId);
//      }
//    } catch (Exception e) {
//      log.error("Error handling session connected event: {}", e.getMessage());
//    }
//  }
//
//  public void onSessionDisconnect(SessionDisconnectEvent event) {
//    try {
//      StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//      String sessionId = accessor.getSessionId();
//
//      userConnections
//          .entrySet()
//          .removeIf(
//              entry -> {
//                if (entry.getValue().getSessionId().equals(sessionId)) {
//                  totalConnections.decrementAndGet();
//                  return true;
//                }
//                return false;
//              });
//    } catch (Exception e) {
//      log.error("Error handling session disconnect event: {}", e.getMessage());
//    }
//  }
//
//  public void onSessionSubscribe(SessionSubscribeEvent event) {
//    try {
//      StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//      String sessionId = accessor.getSessionId();
//
//      userConnections.values().stream()
//          .filter(info -> info.getSessionId().equals(sessionId))
//          .findFirst()
//          .ifPresent(UserConnectionInfo::incrementSubscriptions);
//
//    } catch (Exception e) {
//      log.error("Error handling session subscribe event: {}", e.getMessage());
//    }
//  }
//
//  public void onSessionUnsubscribe(SessionUnsubscribeEvent event) {
//    try {
//      StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//      String sessionId = accessor.getSessionId();
//
//      userConnections.values().stream()
//          .filter(info -> info.getSessionId().equals(sessionId))
//          .findFirst()
//          .ifPresent(UserConnectionInfo::decrementSubscriptions);
//
//    } catch (Exception e) {
//      log.error("Error handling session unsubscribe event: {}", e.getMessage());
//    }
//  }
//
//  public void cleanupExpiredConnections() {
//    try {
//      int beforeSize = userConnections.size();
//      int beforeTotal = totalConnections.get();
//
//      userConnections
//          .entrySet()
//          .removeIf(
//              entry -> {
//                if (entry.getValue().isExpired()) {
//                  totalConnections.decrementAndGet();
//                  return true;
//                }
//                return false;
//              });
//
//      int afterSize = userConnections.size();
//      int afterTotal = totalConnections.get();
//
//      if (beforeSize != afterSize || beforeTotal != afterTotal) {
//        log.info(
//            "Cleaned up expired connections: users {}->{}, total {}->{}",
//            beforeSize,
//            afterSize,
//            beforeTotal,
//            afterTotal);
//      }
//
//    } catch (Exception e) {
//      log.error("Error cleaning up expired connections: {}", e.getMessage());
//    }
//  }
//
//  public int getTotalConnections() {
//    return totalConnections.get();
//  }
//
//  public int getUserConnectionCount() {
//    return userConnections.size();
//  }
//
//  public Map<String, UserConnectionInfo> getUserConnections() {
//    return new ConcurrentHashMap<>(userConnections);
//  }
//
//  public boolean isHealthy() {
//    return totalConnections.get() <= MAX_TOTAL_CONNECTIONS;
//  }
// }
