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
      log.error("Unable to send email due to template loading failure.");
      return;
    }

    // Replace {{OTP_CODE}} with actual OTP value
    Map<String, String> valuesMap = new HashMap<>();
    valuesMap.put("OTP_CODE", otp);

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);
    String finalForm = substitutor.replace(form);

    try {
      HtmlEmail email = new HtmlEmail();
      email.setHostName(host);
      email.setSmtpPort(port);
      email.setAuthentication(username, password);
      email.setStartTLSRequired(true);
      email.setSSLOnConnect(true); // Enable SSL for security
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
