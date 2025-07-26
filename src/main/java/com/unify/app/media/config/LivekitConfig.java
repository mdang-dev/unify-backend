package com.unify.app.media.config;

import com.unify.app.ApplicationProperties;
import io.livekit.server.IngressServiceClient;
import io.livekit.server.RoomServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LivekitConfig {

  private final ApplicationProperties properties;

  @Bean
  public RoomServiceClient roomServiceClient() {
    return RoomServiceClient.createClient(
        properties.livekitHost(), properties.livekitApiKey(), properties.livekitApiSecret());
  }

  @Bean
  public IngressServiceClient ingressServiceClient() {
    return IngressServiceClient.Companion.createClient(
        properties.livekitHost(), properties.livekitApiKey(), properties.livekitApiSecret());
  }
}
