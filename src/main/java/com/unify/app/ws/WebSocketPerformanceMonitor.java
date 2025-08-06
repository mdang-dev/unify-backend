package com.unify.app.ws;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketPerformanceMonitor {

  private final AtomicLong totalMessagesSent = new AtomicLong(0);
  private final AtomicLong totalMessagesReceived = new AtomicLong(0);
  private final AtomicInteger activeConnections = new AtomicInteger(0);
  private final AtomicLong totalConnectionTime = new AtomicLong(0);
  private final AtomicLong messageProcessingTime = new AtomicLong(0);

  public void incrementMessagesSent() {
    totalMessagesSent.incrementAndGet();
  }

  public void incrementMessagesReceived() {
    totalMessagesReceived.incrementAndGet();
  }

  public void incrementActiveConnections() {
    activeConnections.incrementAndGet();
    log.debug("Active connections: {}", activeConnections.get());
  }

  public void decrementActiveConnections() {
    activeConnections.decrementAndGet();
    log.debug("Active connections: {}", activeConnections.get());
  }

  public void addConnectionTime(long connectionTimeMs) {
    totalConnectionTime.addAndGet(connectionTimeMs);
  }

  public void addMessageProcessingTime(long processingTimeMs) {
    messageProcessingTime.addAndGet(processingTimeMs);
  }

  public void logPerformanceMetrics() {
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
