// package com.unify.app.ws;
//
// import java.util.concurrent.atomic.AtomicInteger;
// import java.util.concurrent.atomic.AtomicLong;
// import java.util.concurrent.atomic.AtomicReference;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
//
// @Component
// public class WebSocketPerformanceMonitor {
//
//  private static final Logger log = LoggerFactory.getLogger(WebSocketPerformanceMonitor.class);
//
//  private final AtomicLong totalMessagesSent = new AtomicLong(0);
//  private final AtomicLong totalMessagesReceived = new AtomicLong(0);
//  private final AtomicInteger activeConnections = new AtomicInteger(0);
//  private final AtomicLong totalConnectionTime = new AtomicLong(0);
//  private final AtomicLong messageProcessingTime = new AtomicLong(0);
//
//  // ✅ NEW: Enhanced performance metrics
//  private final AtomicLong avgLatency = new AtomicLong(0);
//  private final AtomicLong maxLatency = new AtomicLong(0);
//  private final AtomicLong minLatency = new AtomicLong(Long.MAX_VALUE);
//  private final AtomicInteger connectionErrors = new AtomicInteger(0);
//  private final AtomicInteger authenticationFailures = new AtomicInteger(0);
//  private final AtomicLong totalLatency = new AtomicLong(0);
//  private final AtomicLong latencyCount = new AtomicLong(0);
//
//  private final AtomicReference<Long> lastMetricsLog =
//      new AtomicReference<>(System.currentTimeMillis());
//
//  private static final long METRICS_LOG_INTERVAL = 300000; // 5 minutes
//  private static final int MAX_ACTIVE_CONNECTIONS = 2000;
//
//  // ✅ NEW: Performance thresholds for alerts
//  private static final long HIGH_LATENCY_THRESHOLD = 1000; // 1 second
//  private static final int HIGH_CONNECTION_THRESHOLD = 1500; // 75% of max
//  private static final int HIGH_ERROR_THRESHOLD = 100; // Error rate threshold
//
//  public void incrementMessagesSent() {
//    totalMessagesSent.incrementAndGet();
//  }
//
//  public void incrementMessagesReceived() {
//    totalMessagesReceived.incrementAndGet();
//  }
//
//  public void incrementActiveConnections() {
//    int current = activeConnections.incrementAndGet();
//
//    if (current > MAX_ACTIVE_CONNECTIONS) {
//      // Only log critical connection limit issues
//      log.error("Active connections limit reached: {}", current);
//      activeConnections.decrementAndGet();
//      return;
//    }
//  }
//
//  public void decrementActiveConnections() {
//    int current = activeConnections.decrementAndGet();
//    if (current < 0) {
//      activeConnections.set(0);
//      current = 0;
//    }
//  }
//
//  public void addConnectionTime(long connectionTimeMs) {
//    if (connectionTimeMs > 0 && connectionTimeMs < 120000) {
//      totalConnectionTime.addAndGet(connectionTimeMs);
//    }
//  }
//
//  public void addMessageProcessingTime(long processingTimeMs) {
//    if (processingTimeMs > 0 && processingTimeMs < 60000) {
//      messageProcessingTime.addAndGet(processingTimeMs);
//    }
//  }
//
//  // ✅ NEW: Add latency measurement
//  public void addLatencyMeasurement(long latencyMs) {
//    if (latencyMs > 0 && latencyMs < 30000) { // Max 30 seconds
//      totalLatency.addAndGet(latencyMs);
//      latencyCount.incrementAndGet();
//
//      // Update min/max latency
//      long currentMin = minLatency.get();
//      while (latencyMs < currentMin && !minLatency.compareAndSet(currentMin, latencyMs)) {
//        currentMin = minLatency.get();
//      }
//
//      long currentMax = maxLatency.get();
//      while (latencyMs > currentMax && !maxLatency.compareAndSet(currentMax, latencyMs)) {
//        currentMax = maxLatency.get();
//      }
//
//      // Update average latency
//      long total = totalLatency.get();
//      long count = latencyCount.get();
//      if (count > 0) {
//        avgLatency.set(total / count);
//      }
//    }
//  }
//
//  // ✅ NEW: Increment error counters
//  public void incrementConnectionErrors() {
//    connectionErrors.incrementAndGet();
//  }
//
//  public void incrementAuthenticationFailures() {
//    authenticationFailures.incrementAndGet();
//  }
//
//  // ✅ NEW: Performance alerts - only log critical issues
//  @Scheduled(fixedRate = 30000) // Every 30 seconds
//  public void checkPerformanceAlerts() {
//    int connections = activeConnections.get();
//    long avgLat = avgLatency.get();
//    int errors = connectionErrors.get();
//    int authFailures = authenticationFailures.get();
//
//    // Only log critical alerts
//    if (connections > HIGH_CONNECTION_THRESHOLD) {
//      log.error(
//          "HIGH CONNECTION COUNT: {} connections ({}% of max)",
//          connections, (connections * 100) / MAX_ACTIVE_CONNECTIONS);
//    }
//
//    if (avgLat > HIGH_LATENCY_THRESHOLD) {
//      log.error(
//          "HIGH LATENCY: Average latency {}ms exceeds threshold {}ms",
//          avgLat,
//          HIGH_LATENCY_THRESHOLD);
//    }
//
//    if (errors > HIGH_ERROR_THRESHOLD) {
//      log.error("HIGH ERROR RATE: {} connection errors in last period", errors);
//    }
//
//    if (authFailures > HIGH_ERROR_THRESHOLD / 2) {
//      log.error("HIGH AUTH FAILURES: {} auth failures in last period", authFailures);
//    }
//
//    // Reset error counters after alerting
//    if (errors > 0) {
//      connectionErrors.set(0);
//    }
//    if (authFailures > 0) {
//      authenticationFailures.set(0);
//    }
//  }
//
//  @Scheduled(fixedRate = 300000)
//  public void logPerformanceMetrics() {
//    long currentTime = System.currentTimeMillis();
//    Long lastLog = lastMetricsLog.get();
//
//    if (lastLog == null || (currentTime - lastLog) >= METRICS_LOG_INTERVAL) {
//      lastMetricsLog.set(currentTime);
//
//      long sent = totalMessagesSent.get();
//      long received = totalMessagesReceived.get();
//      int connections = activeConnections.get();
//      long avgLat = avgLatency.get();
//      long maxLat = maxLatency.get();
//      long minLat = minLatency.get();
//
//      // Only log if there are issues or high activity
//      if (connections > HIGH_CONNECTION_THRESHOLD
//          || avgLat > HIGH_LATENCY_THRESHOLD
//          || connectionErrors.get() > HIGH_ERROR_THRESHOLD) {
//        log.warn(
//            "WebSocket Performance Issues - " + "Connections: {}, Avg Latency: {}ms, Errors: {}",
//            connections,
//            avgLat,
//            connectionErrors.get());
//      }
//    }
//  }
//
//  public boolean isHealthy() {
//    int connections = activeConnections.get();
//    long avgLat = avgLatency.get();
//    int errors = connectionErrors.get();
//
//    return connections >= 0
//        && connections <= MAX_ACTIVE_CONNECTIONS
//        && avgLat <= HIGH_LATENCY_THRESHOLD
//        && errors <= HIGH_ERROR_THRESHOLD;
//  }
//
//  public void resetMetrics() {
//    totalMessagesSent.set(0);
//    totalMessagesReceived.set(0);
//    activeConnections.set(0);
//    totalConnectionTime.set(0);
//    messageProcessingTime.set(0);
//    lastMetricsLog.set(System.currentTimeMillis());
//
//    // ✅ NEW: Reset enhanced metrics
//    avgLatency.set(0);
//    maxLatency.set(0);
//    minLatency.set(Long.MAX_VALUE);
//    connectionErrors.set(0);
//    authenticationFailures.set(0);
//    totalLatency.set(0);
//    latencyCount.set(0);
//  }
//
//  public long getTotalMessagesSent() {
//    return totalMessagesSent.get();
//  }
//
//  public long getTotalMessagesReceived() {
//    return totalMessagesReceived.get();
//  }
//
//  public int getActiveConnections() {
//    return activeConnections.get();
//  }
//
//  // ✅ NEW: Get enhanced metrics
//  public long getAvgLatency() {
//    return avgLatency.get();
//  }
//
//  public long getMaxLatency() {
//    return maxLatency.get();
//  }
//
//  public long getMinLatency() {
//    long min = minLatency.get();
//    return min == Long.MAX_VALUE ? 0 : min;
//  }
//
//  public int getConnectionErrors() {
//    return connectionErrors.get();
//  }
//
//  public int getAuthenticationFailures() {
//    return authenticationFailures.get();
//  }
//
//  public String getHealthStatus() {
//    if (isHealthy()) {
//      return "HEALTHY";
//    } else if (activeConnections.get() > HIGH_CONNECTION_THRESHOLD) {
//      return "HIGH_LOAD";
//    } else if (avgLatency.get() > HIGH_LATENCY_THRESHOLD) {
//      return "HIGH_LATENCY";
//    } else if (connectionErrors.get() > HIGH_ERROR_THRESHOLD) {
//      return "HIGH_ERROR_RATE";
//    } else {
//      return "DEGRADED";
//    }
//  }
// }
