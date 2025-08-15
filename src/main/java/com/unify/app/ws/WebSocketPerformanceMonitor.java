package com.unify.app.ws;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketPerformanceMonitor {

  private final AtomicLong totalMessagesSent = new AtomicLong(0);
  private final AtomicLong totalMessagesReceived = new AtomicLong(0);
  private final AtomicInteger activeConnections = new AtomicInteger(0);
  private final AtomicLong totalConnectionTime = new AtomicLong(0);
  private final AtomicLong messageProcessingTime = new AtomicLong(0);

  private final AtomicReference<Long> lastMetricsLog =
      new AtomicReference<>(System.currentTimeMillis());

  private static final long METRICS_LOG_INTERVAL = 300000;
  private static final int MAX_ACTIVE_CONNECTIONS = 2000;

  public void incrementMessagesSent() {
    totalMessagesSent.incrementAndGet();
  }

  public void incrementMessagesReceived() {
    totalMessagesReceived.incrementAndGet();
  }

  public void incrementActiveConnections() {
    int current = activeConnections.incrementAndGet();

    if (current > MAX_ACTIVE_CONNECTIONS) {
      log.warn("Active connections limit reached: {}", current);
      activeConnections.decrementAndGet();
      return;
    }
  }

  public void decrementActiveConnections() {
    int current = activeConnections.decrementAndGet();
    if (current < 0) {
      activeConnections.set(0);
      current = 0;
    }
  }

  public void addConnectionTime(long connectionTimeMs) {
    if (connectionTimeMs > 0 && connectionTimeMs < 120000) {
      totalConnectionTime.addAndGet(connectionTimeMs);
    }
  }

  public void addMessageProcessingTime(long processingTimeMs) {
    if (processingTimeMs > 0 && processingTimeMs < 60000) {
      messageProcessingTime.addAndGet(processingTimeMs);
    }
  }

  @Scheduled(fixedRate = 300000)
  public void logPerformanceMetrics() {
    long currentTime = System.currentTimeMillis();
    Long lastLog = lastMetricsLog.get();

    if (lastLog == null || (currentTime - lastLog) >= METRICS_LOG_INTERVAL) {
      lastMetricsLog.set(currentTime);
      
      long sent = totalMessagesSent.get();
      long received = totalMessagesReceived.get();
      int connections = activeConnections.get();

      log.info(
          "WebSocket Performance Metrics - "
              + "Sent: {}, Received: {}, Active Connections: {}, "
              + "Avg Connection Time: {}ms, Avg Processing Time: {}ms",
          sent,
          received,
          connections,
          connections > 0 ? totalConnectionTime.get() / connections : 0,
          (sent + received) > 0 ? messageProcessingTime.get() / (sent + received) : 0);
    }
  }

  public boolean isHealthy() {
    int connections = activeConnections.get();
    return connections >= 0 && connections <= MAX_ACTIVE_CONNECTIONS;
  }

  public void resetMetrics() {
    totalMessagesSent.set(0);
    totalMessagesReceived.set(0);
    activeConnections.set(0);
    totalConnectionTime.set(0);
    messageProcessingTime.set(0);
    lastMetricsLog.set(System.currentTimeMillis());
  }

  public long getTotalMessagesSent() {
    return totalMessagesSent.get();
  }

  public long getTotalMessagesReceived() {
    return totalMessagesReceived.get();
  }

  public int getActiveConnections() {
    return activeConnections.get();
  }
}
