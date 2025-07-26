package com.unify.app.media.web.webhook;

import com.unify.app.ApplicationProperties;
import com.unify.app.media.domain.StreamService;
import io.livekit.server.WebhookReceiver;
import io.swagger.v3.oas.annotations.Hidden;
import livekit.LivekitWebhook.WebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/webhooks/livekit")
@RequiredArgsConstructor
@Slf4j
class LivekitWebhook {

  private final ApplicationProperties properties;
  private final StreamService streamService;

  @PostMapping
  public ResponseEntity<Void> handlelivekitWebhook(
      @RequestBody String body,
      @RequestHeader(value = "Authorization", required = false) String authorization) {

    WebhookReceiver receiver =
        new WebhookReceiver(properties.livekitApiKey(), properties.livekitApiSecret());

    if (authorization == null) {
      return ResponseEntity.status(400).build();
    }

    WebhookEvent event = receiver.receive(body, authorization);

    if ("ingress_ended".equals(event.getEvent()) && event.hasIngressInfo()) {
      streamService.updateIsLiveStream(event.getIngressInfo().getIngressId(), false);
    }

    if ("ingress_started".equals(event.getEvent()) && event.hasIngressInfo()) {
      streamService.updateIsLiveStream(event.getIngressInfo().getIngressId(), true);
    }

    return ResponseEntity.ok().build();
  }
}
