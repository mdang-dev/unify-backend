package com.unify.app.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ws/health")
@RequiredArgsConstructor
public class WebSocketHealthController {

  private final WebSocketPerformanceMonitor performanceMonitor;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getWebSocketHealth() {
    try {
      Map<String, Object> health = new HashMap<>();
      
      health.put("status", performanceMonitor.isHealthy() ? "UP" : "DOWN");
      health.put("timestamp", System.currentTimeMillis());
      
      health.put("activeConnections", performanceMonitor.getActiveConnections());
      health.put("totalMessagesSent", performanceMonitor.getTotalMessagesSent());
      health.put("totalMessagesReceived", performanceMonitor.getTotalMessagesReceived());
      
      health.put("maxConnections", 1000);
      health.put("maxConnectionsPerUser", 5);
      
      long estimatedMemoryUsage = performanceMonitor.getActiveConnections() * 1024;
      health.put("estimatedMemoryUsageKB", estimatedMemoryUsage);
      
      boolean isHealthy = performanceMonitor.isHealthy();
      health.put("healthy", isHealthy);
      
      if (isHealthy) {
        return ResponseEntity.ok(health);
      } else {
        return ResponseEntity.status(503).body(health);
      }
      
    } catch (Exception e) {
      log.error("Error getting WebSocket health: {}", e.getMessage());
      
      Map<String, Object> error = new HashMap<>();
      error.put("status", "ERROR");
      error.put("error", e.getMessage());
      error.put("timestamp", System.currentTimeMillis());
      
      return ResponseEntity.status(500).body(error);
    }
  }

  @PostMapping("/reset")
  public ResponseEntity<Map<String, String>> resetMetrics() {
    try {
      performanceMonitor.resetMetrics();
      
      Map<String, String> response = new HashMap<>();
      response.put("status", "SUCCESS");
      response.put("message", "WebSocket metrics reset successfully");
      response.put("timestamp", String.valueOf(System.currentTimeMillis()));
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("Error resetting WebSocket metrics: {}", e.getMessage());
      
      Map<String, String> error = new HashMap<>();
      error.put("status", "ERROR");
      error.put("error", e.getMessage());
      error.put("timestamp", String.valueOf(System.currentTimeMillis()));
      
      return ResponseEntity.status(500).body(error);
    }
  }

  @GetMapping("/status")
  public ResponseEntity<Map<String, String>> getSimpleStatus() {
    Map<String, String> status = new HashMap<>();
    status.put("status", performanceMonitor.isHealthy() ? "UP" : "DOWN");
    status.put("activeConnections", String.valueOf(performanceMonitor.getActiveConnections()));
    status.put("timestamp", String.valueOf(System.currentTimeMillis()));
    
    return ResponseEntity.ok(status);
  }
} 