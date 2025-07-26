package com.unify.app.users.domain.mail;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

  private final Map<String, String> otpCache = new ConcurrentHashMap<>();
  private final Map<String, Boolean> otpValidated = new ConcurrentHashMap<>();

  public String generateOtp(String email) {
    String otp = OtpGenerator.generatorOTP();
    otpCache.put(email, otp);
    otpValidated.put(email, false);
    return otp;
  }

  public boolean validateOtp(String email, String otp) {
    // return otpCache.containsKey(email) && otpCache.get(email).equals(otp);
    if (otpCache.equals(otpCache.get(email))) {
      otpValidated.put(otp, true);
      return true;
    }
    return false;
  }

  public boolean isOtpValidated(String email) {
    return otpValidated.getOrDefault(email, false);
  }

  // Xóa OTP sau 30s
  @Scheduled(fixedRate = 30000) // 30,000 ms = 30s
  public void clearExpiredOtps() {
    otpCache.clear();
  }

  public void clearOTP(String email) {
    otpCache.remove(email);
    otpValidated.remove(email);
  }
}
