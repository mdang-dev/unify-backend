package com.unify.app.users.domain.mail;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OtpService {

  private final Map<String, OtpData> otpCache = new ConcurrentHashMap<>();
  private final Map<String, Boolean> otpValidated = new ConcurrentHashMap<>();

  // ✅ OPTIMIZED: Add size limits to prevent memory overflow
  private static final int MAX_OTP_CACHE_SIZE = 1000;
  private static final long OTP_EXPIRY_TIME_MS = 30000; // 30 seconds

  private static class OtpData {
    final String otp;
    final long createdAt;

    OtpData(String otp) {
      this.otp = otp;
      this.createdAt = System.currentTimeMillis();
    }

    boolean isExpired() {
      return System.currentTimeMillis() - createdAt > OTP_EXPIRY_TIME_MS;
    }
  }

  public String generateOtp(String email) {
    // ✅ OPTIMIZED: Check cache size before adding new OTP
    if (otpCache.size() >= MAX_OTP_CACHE_SIZE) {
      log.warn("OTP cache size limit reached, clearing expired entries");
      clearExpiredOtps();

      // If still at limit, remove oldest entries
      if (otpCache.size() >= MAX_OTP_CACHE_SIZE) {
        clearOldestEntries();
      }
    }

    String otp = OtpGenerator.generatorOTP();
    otpCache.put(email, new OtpData(otp));
    otpValidated.put(email, false);
    return otp;
  }

  public boolean validateOtp(String email, String otp) {
    OtpData otpData = otpCache.get(email);
    if (otpData != null && !otpData.isExpired() && otpData.otp.equals(otp)) {
      otpValidated.put(email, true);
      return true;
    }
    return false;
  }

  public boolean isOtpValidated(String email) {
    return otpValidated.getOrDefault(email, false);
  }

  // ✅ OPTIMIZED: Smart cleanup that only removes expired OTPs
  @Scheduled(fixedRate = 30000) // 30,000 ms = 30s
  public void clearExpiredOtps() {
    try {
      int beforeSize = otpCache.size();
      otpCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
      int afterSize = otpCache.size();

      if (beforeSize != afterSize) {
        // Cache cleanup completed
      }
    } catch (Exception e) {
      log.error("Error clearing expired OTPs: {}", e.getMessage());
    }
  }

  // ✅ OPTIMIZED: Remove oldest entries if cache is still too large
  private void clearOldestEntries() {
    try {
      int targetSize = MAX_OTP_CACHE_SIZE / 2; // Reduce to half
      int toRemove = otpCache.size() - targetSize;

      if (toRemove > 0) {
        otpCache.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e1.getValue().createdAt, e2.getValue().createdAt))
            .limit(toRemove)
            .forEach(
                entry -> {
                  otpCache.remove(entry.getKey());
                  otpValidated.remove(entry.getKey());
                });
      }
    } catch (Exception e) {
      log.error("Error clearing oldest OTP entries: {}", e.getMessage());
    }
  }

  public void clearOTP(String email) {
    otpCache.remove(email);
    otpValidated.remove(email);
  }

  // ✅ OPTIMIZED: Add monitoring methods
  public int getCacheSize() {
    return otpCache.size();
  }

  public int getValidatedCount() {
    return otpValidated.size();
  }
}
