package com.unify.app.users.domain.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApacheMailService {

  @Value("${mail.host}")
  private String host;

  @Value("${mail.port}")
  private int port;

  @Value("${mail.username}")
  private String username;

  @Value("${mail.password}")
  private String password;

  @Async // Send email asynchronously
  public void sendMail(String to, String otp) {
    String subject = "Confirm OTP from our service";
    String form = loadEmailTemplate("templates/email-otp-form.html");

    if (form == null) {
      // Fallback to a minimal inline template to avoid breaking flow
      log.warn("Template not found, using fallback inline template.");
      form =
          "<html><body>"
              + "<h3>Your One-Time Password (OTP)</h3>"
              + "<p><strong>${OTP_CODE}</strong></p>"
              + "<p>If you did not request this, please ignore this email.</p>"
              + "</body></html>";
    }

    // Replace {{OTP_CODE}} with actual OTP value
    Map<String, String> valuesMap = new HashMap<>();
    valuesMap.put("OTP_CODE", otp);
    valuesMap.put("YEAR", String.valueOf(java.time.Year.now().getValue()));

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);
    String finalForm = substitutor.replace(form);

    try {
      HtmlEmail email = new HtmlEmail();
      email.setHostName(host);
      email.setSmtpPort(port);
      email.setAuthentication(username, password);
      // Configure TLS/SSL depending on port
      // 587 typically uses STARTTLS, 465 typically uses SSL
      if (port == 465) {
        email.setSSLOnConnect(true);
      } else {
        email.setStartTLSEnabled(true);
      }
      // Set UTF-8 encoding for proper character support
      email.setCharset("UTF-8");

      email.setFrom(username);
      email.setSubject(subject);
      email.setHtmlMsg(finalForm);
      email.addTo(to);
      email.send();
      log.info("OTP has been sent to {}", to);
    } catch (EmailException e) {
      log.error("Error sending email to {}: {}", to, e.getMessage());
    }
  }

  /**
   * Send report notification email with custom subject and HTML content
   *
   * @param to Recipient email address
   * @param subject Email subject
   * @param htmlContent HTML content of the email
   */
  @Async
  public void sendReportNotificationEmail(String to, String subject, String htmlContent) {
    try {
      HtmlEmail email = new HtmlEmail();
      email.setHostName(host);
      email.setSmtpPort(port);
      email.setAuthentication(username, password);

      // Configure TLS/SSL depending on port
      if (port == 465) {
        email.setSSLOnConnect(true);
      } else {
        email.setStartTLSEnabled(true);
      }

      // Set UTF-8 encoding for proper character support
      email.setCharset("UTF-8");

      email.setFrom(username);
      email.setSubject(subject);
      email.setHtmlMsg(htmlContent);
      email.addTo(to);
      email.send();

      log.info("Report notification email sent successfully to {}", to);
    } catch (EmailException e) {
      log.error("Error sending report notification email to {}: {}", to, e.getMessage(), e);
      // Don't throw exception to avoid breaking the main flow
    }
  }

  private String loadEmailTemplate(String filePath) {
    try (InputStreamReader reader =
        new InputStreamReader(
            new ClassPathResource(filePath).getInputStream(), StandardCharsets.UTF_8)) {
      return new BufferedReader(reader).lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      log.error("Unable to load template {}!", filePath, e);
      return null;
    }
  }
}
