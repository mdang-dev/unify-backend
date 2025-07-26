package com.unify.app.users.domain.mail;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class OtpGenerator {
  private static final int OTP_LENGTH = 6;

  public static String generatorOTP() {
    SecureRandom random = new SecureRandom();
    StringBuilder otp = new StringBuilder();

    for (int i = 0; i < OTP_LENGTH; i++) {
      int digit = random.nextInt(10);
      otp.append(digit);
    }
    return "G-" + otp.toString();
  }
}
