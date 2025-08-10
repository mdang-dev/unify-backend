package com.unify.app.users.web;

import com.unify.app.security.CustomLogoutHandler;
import com.unify.app.users.domain.AuthService;
import com.unify.app.users.domain.UserService;
import com.unify.app.users.domain.mail.ApacheMailService;
import com.unify.app.users.domain.mail.OtpService;
import com.unify.app.users.domain.models.ApiResponse;
import com.unify.app.users.domain.models.UserDto;
import com.unify.app.users.domain.models.auth.CreateUserCmd;
import com.unify.app.users.domain.models.auth.ForgotPasswordRequest;
import com.unify.app.users.domain.models.auth.ResetPasswordRequest;
import com.unify.app.users.domain.models.auth.UserLoginCmd;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
class AuthController {

  private final AuthService authService;
  private final UserService userService;
  private final ApacheMailService apacheMailService;
  private final OtpService otpService;
  private final PasswordEncoder passwordEncoder;
  private final CustomLogoutHandler customLogoutHandler;

  @PostMapping("/login")
  ResponseEntity<?> login(@RequestBody UserLoginCmd userLoginDto) {
    var res = authService.login(userLoginDto);
    if (res != null) {
      return ResponseEntity.ok(res);
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect email or password !");
  }

  @PostMapping("/register")
  ResponseEntity<String> register(@RequestBody @Valid CreateUserCmd cmd) {
    if (userService.existsByEmail(cmd.email())) {
      return new ResponseEntity<>("Email is taken !", HttpStatus.BAD_REQUEST);
    } else if (userService.existsByUsername(cmd.username())) {
      return new ResponseEntity<>("Username is taken !", HttpStatus.BAD_REQUEST);
    } else {
      userService.createUser(cmd);
      return new ResponseEntity<>("Register successfully !", HttpStatus.CREATED);
    }
  }

  // @GetMapping("/users/refresh")
  // ResponseEntity<Object> refreshToken() {
  // String email =
  // SecurityContextHolder.getContext().getAuthentication().getName();
  // var user = userService.findByEmail(email);
  // String token = jwtService.generateToken(email);
  // authenticationService.saveUserToken(user, token);
  // return ResponseEntity.status(200).body(new TokenResponse(token));
  // }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    customLogoutHandler.logout(request, response, authentication);
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok().build();
  }

  @GetMapping("/csrf")
  ResponseEntity<Map<String, String>> getCsrfToken(HttpServletRequest request) {
    // Generate a simple CSRF token for WebSocket connections
    String csrfToken = java.util.UUID.randomUUID().toString();
    return ResponseEntity.ok(Map.of("token", csrfToken));
  }

  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(
      @RequestBody UserDto userDto, HttpServletRequest request) {
    String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    int failedAttempts = authService.getFailedAttempts(userEmail);

    if (failedAttempts >= 5) {
      authService.clearFailedAttempts(userEmail);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              Map.of(
                  "message", "Too many failed attempts! Please log in again.", "action", "logout"));
    }

    try {
      userService.changePassword(userDto.currentPassword(), userDto.newPassword());
      authService.clearFailedAttempts(userEmail);
      return ResponseEntity.ok(Map.of("message", "Password changed successfully!"));
    } catch (IllegalArgumentException e) {
      authService.incrementFailedAttempts(userEmail);
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/forgot-password/send-mail")
  public ResponseEntity<ApiResponse> sendMail(
      @RequestBody @Validated ForgotPasswordRequest request) {
    String email = request.email();

    if (!userService.existsByEmail(email)) {
      return ResponseEntity.status(404).body(new ApiResponse(false, "Email not found!"));
    }

    String otp = otpService.generateOtp(email);

    apacheMailService.sendMail(email, otp);
    log.info("OTP sent successfully to {}", email);
    return ResponseEntity.ok(new ApiResponse(true, "OTP sent successfully!"));
  }

  @PostMapping("/forgot-password/otp-verification")
  public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    String otp = request.get("otp");

    if (otpService.validateOtp(email, otp)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid OTP!"));
    }

    otpService.clearExpiredOtps(); // Xóa OTP sau khi xác thực thành công
    return ResponseEntity.ok(Map.of("message", "OTP verified!"));
  }

  @PostMapping("/forgot-password/reset-password")
  public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {

    String email = request.email();
    String newPassword = request.newPassword();

    if (!userService.existsByEmail(email)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Email not found!"));
    }

    if (otpService.isOtpValidated(email)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", "OTP not verified!"));
    }

    String encryptPassword = encryptPassword(newPassword);

    userService.updatePassword(email, encryptPassword);

    otpService.clearOTP(email);

    return ResponseEntity.ok(Map.of("message", "Password reset successfully!"));
  }

  private String encryptPassword(String password) {
    return passwordEncoder.encode(password);
  }
}
