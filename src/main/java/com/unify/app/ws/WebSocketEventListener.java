// package com.unify.app.ws;
//
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.context.event.EventListener;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
// import org.springframework.web.socket.messaging.SessionConnectedEvent;
// import org.springframework.web.socket.messaging.SessionDisconnectEvent;
// import org.springframework.web.socket.messaging.SessionSubscribeEvent;
// import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
//
// @Component
// public class WebSocketEventListener {
//
//  private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
//
//  private final WebSocketConnectionManager connectionManager;
//  private final WebSocketPerformanceMonitor performanceMonitor;
//
//  // ✅ NEW: Constructor for dependency injection
//  public WebSocketEventListener(
//      WebSocketConnectionManager connectionManager,
//      WebSocketPerformanceMonitor performanceMonitor) {
//    this.connectionManager = connectionManager;
//    this.performanceMonitor = performanceMonitor;
//  }
//
//  @EventListener
//  public void handleSessionConnected(SessionConnectedEvent event) {
//    try {
//      connectionManager.onSessionConnected(event);
//    } catch (Exception e) {
//      log.error("Error processing session connected event: {}", e.getMessage());
//    }
//  }
//
//  @EventListener
//  public void handleSessionDisconnect(SessionDisconnectEvent event) {
//    try {
//      connectionManager.onSessionDisconnect(event);
//      performanceMonitor.decrementActiveConnections();
//    } catch (Exception e) {
//      log.error("Error processing session disconnect event: {}", e.getMessage());
//    }
//  }
//
//  @EventListener
//  public void handleSessionSubscribe(SessionSubscribeEvent event) {
//    try {
//      connectionManager.onSessionSubscribe(event);
//    } catch (Exception e) {
//      log.error("Error processing session subscribe event: {}", e.getMessage());
//    }
//  }
//
//  @EventListener
//  public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
//    try {
//      connectionManager.onSessionUnsubscribe(event);
//    } catch (Exception e) {
//      log.error("Error processing session unsubscribe event: {}", e.getMessage());
//    }
//  }
//
//  @Scheduled(fixedRate = 60000)
//  public void cleanupExpiredConnections() {
//    try {
//      connectionManager.cleanupExpiredConnections();
//    } catch (Exception e) {
//      log.error("Error during scheduled connection cleanup: {}", e.getMessage());
//    }
//  }
//
//  @Scheduled(fixedRate = 300000)
//  public void checkWebSocketHealth() {
//    try {
//      boolean isHealthy = connectionManager.isHealthy();
//      if (!isHealthy) {
//        // Only log critical health issues
//        log.error(
//            "WebSocket health check failed - too many connections: {}",
//            connectionManager.getTotalConnections());
//      }
//    } catch (Exception e) {
//      log.error("Error during WebSocket health check: {}", e.getMessage());
//    }
//  }
// }
