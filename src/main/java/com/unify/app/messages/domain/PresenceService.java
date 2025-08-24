package com.unify.app.messages.domain;

import com.unify.app.common.utils.DateTimeUtils;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class PresenceService {

  private final Map<String, Boolean> activeUsers = new ConcurrentHashMap<>();
  private final Map<String, LocalDateTime> lastActive = new ConcurrentHashMap<>();
  private final Map<String, String> typingTo = new ConcurrentHashMap<>();

  public void setActive(String userId) {
    activeUsers.put(userId, true);
    lastActive.put(userId, DateTimeUtils.nowVietnam());
  }

  public void setInactive(String userId) {
    activeUsers.put(userId, false);
    lastActive.put(userId, DateTimeUtils.nowVietnam());
  }

  public boolean isActive(String userId) {
    return activeUsers.getOrDefault(userId, false);
  }

  public LocalDateTime getLastActive(String userId) {
    return lastActive.getOrDefault(userId, DateTimeUtils.nowVietnam());
  }

  public void setTyping(String fromUser, String toUser) {
    typingTo.put(fromUser, toUser);
  }

  public void clearTyping(String fromUser) {
    typingTo.remove(fromUser);
  }

  public String typingTo(String fromUser) {
    return typingTo.get(fromUser);
  }

  // === New Methods for Online Users Feature ===

  public Set<String> getOnlineUsers() {
    // return only users marked as active
    return activeUsers.entrySet().stream()
        .filter(Map.Entry::getValue)
        .map(Map.Entry::getKey)
        .collect(java.util.stream.Collectors.toSet());
  }
}
