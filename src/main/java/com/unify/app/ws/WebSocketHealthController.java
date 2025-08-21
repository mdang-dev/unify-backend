// package com.unify.app.ws;
//
// import java.util.HashMap;
// import java.util.Map;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
//
// @RestController
// @RequestMapping("/api/ws/health")
// public class WebSocketHealthController {
//
//  private final WebSocketPerformanceMonitor performanceMonitor;
//  private final WebSocketConnectionManager connectionManager;
//
//  // ✅ NEW: Constructor for dependency injection
//  public WebSocketHealthController(
//      WebSocketPerformanceMonitor performanceMonitor,
//      WebSocketConnectionManager connectionManager) {
//    this.performanceMonitor = performanceMonitor;
//    this.connectionManager = connectionManager;
//  }
//
//  @GetMapping
//  public ResponseEntity<Map<String, Object>> getHealthStatus() {
//    Map<String, Object> healthData = new HashMap<>();
//
//    // Basic health status
//    healthData.put("status", performanceMonitor.getHealthStatus());
//    healthData.put("healthy", performanceMonitor.isHealthy());
//
//    // Connection metrics
//    healthData.put("activeConnections", performanceMonitor.getActiveConnections());
//    healthData.put("totalConnections", connectionManager.getTotalConnections());
//    healthData.put("userConnections", connectionManager.getUserConnectionCount());
//
//    // Performance metrics
//    healthData.put("totalMessagesSent", performanceMonitor.getTotalMessagesSent());
//    healthData.put("totalMessagesReceived", performanceMonitor.getTotalMessagesReceived());
//    healthData.put("avgLatency", performanceMonitor.getAvgLatency());
//    healthData.put("maxLatency", performanceMonitor.getMaxLatency());
//    healthData.put("minLatency", performanceMonitor.getMinLatency());
//
//    // Error metrics
//    healthData.put("connectionErrors", performanceMonitor.getConnectionErrors());
//    healthData.put("authenticationFailures", performanceMonitor.getAuthenticationFailures());
//
//    // Timestamp
//    healthData.put("timestamp", System.currentTimeMillis());
//
//    return ResponseEntity.ok(healthData);
//  }
//
//  @GetMapping("/metrics")
//  public ResponseEntity<Map<String, Object>> getDetailedMetrics() {
//    Map<String, Object> metrics = new HashMap<>();
//
//    // Connection details
//    metrics.put("connections", connectionManager.getUserConnections());
//
//    // Performance thresholds
//    metrics.put("maxConnections", 2000);
//    metrics.put("highConnectionThreshold", 1500);
//    metrics.put("highLatencyThreshold", 1000);
//    metrics.put("highErrorThreshold", 100);
//
//    return ResponseEntity.ok(metrics);
//  }
//
//  @GetMapping("/reset")
//  public ResponseEntity<Map<String, String>> resetMetrics() {
//    performanceMonitor.resetMetrics();
//
//    Map<String, String> response = new HashMap<>();
//    response.put("message", "Metrics reset successfully");
//    response.put("timestamp", String.valueOf(System.currentTimeMillis()));
//
//    return ResponseEntity.ok(response);
//  }
// }
