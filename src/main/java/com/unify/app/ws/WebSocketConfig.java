package com.unify.app.ws;

import java.util.concurrent.ThreadPoolExecutor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final WebSocketAuthInterceptor webSocketAuthInterceptor;

  @Override
  public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns(
            "http://localhost:3000",
            "http://localhost:3001",
            "https://unify.qzz.io",
            "https://*.unify.qzz.io",
            "https://unify.id.vn",
            "https://*.unify.id.vn"
            )
        .withSockJS()
        .setHeartbeatTime(25000)
        .setDisconnectDelay(15000)
        .setHttpMessageCacheSize(300)
        .setWebSocketEnabled(true)
        .setSessionCookieNeeded(false);
  }

  @Override
  public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue", "/user");
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
    registry.setPreservePublishOrder(true);
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    ThreadPoolTaskExecutor inboundExecutor = new ThreadPoolTaskExecutor();
    inboundExecutor.setCorePoolSize(4);
    inboundExecutor.setMaxPoolSize(8);
    inboundExecutor.setQueueCapacity(100);
    inboundExecutor.setThreadNamePrefix("ws-inbound-");
    inboundExecutor.setKeepAliveSeconds(45);
    inboundExecutor.setAllowCoreThreadTimeOut(true);
    inboundExecutor.setWaitForTasksToCompleteOnShutdown(true);
    inboundExecutor.setAwaitTerminationSeconds(20);
    inboundExecutor.setRejectedExecutionHandler(
        new ThreadPoolExecutor.CallerRunsPolicy());
    inboundExecutor.initialize();

    registration.interceptors(webSocketAuthInterceptor).taskExecutor(inboundExecutor);
  }

  @Override
  public void configureClientOutboundChannel(ChannelRegistration registration) {
    ThreadPoolTaskExecutor outboundExecutor = new ThreadPoolTaskExecutor();
    outboundExecutor.setCorePoolSize(4);
    outboundExecutor.setMaxPoolSize(8);
    outboundExecutor.setQueueCapacity(100);
    outboundExecutor.setThreadNamePrefix("ws-outbound-");
    outboundExecutor.setKeepAliveSeconds(45);
    outboundExecutor.setAllowCoreThreadTimeOut(true);
    outboundExecutor.setWaitForTasksToCompleteOnShutdown(true);
    outboundExecutor.setAwaitTerminationSeconds(20);
    outboundExecutor.setRejectedExecutionHandler(
        new ThreadPoolExecutor.CallerRunsPolicy());
    outboundExecutor.initialize();

    registration.taskExecutor(outboundExecutor);
  }

  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
    registration
        .setMessageSizeLimit(64 * 1024)
        .setSendBufferSizeLimit(1 * 1024 * 1024)
        .setSendTimeLimit(3000)
        .setTimeToFirstMessage(5000);
  }
}
