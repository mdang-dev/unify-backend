package com.unify.app.ws;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

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
            "http://localhost:3000", // Frontend development
            "http://localhost:3001", // Frontend alternative port
            "https://unify.qzz.io", // Production domain
            "https://*.unify.qzz.io" // Subdomains
        )
        .withSockJS()
        .setHeartbeatTime(8000) // ✅ PERFORMANCE: Faster heartbeat for real-time chat
        .setDisconnectDelay(3000) // ✅ PERFORMANCE: Faster disconnect for better reconnection
        .setHttpMessageCacheSize(2000) // ✅ PERFORMANCE: Larger cache for better performance
        .setWebSocketEnabled(true)
        .setSessionCookieNeeded(false);
  }

  @Override
  public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue", "/user");
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    // ✅ PERFORMANCE: Ultra-optimized ThreadPoolTaskExecutor for real-time chat
    ThreadPoolTaskExecutor inboundExecutor = new ThreadPoolTaskExecutor();
    inboundExecutor.setCorePoolSize(16); // ✅ PERFORMANCE: Doubled for ultra-fast processing
    inboundExecutor.setMaxPoolSize(32); // ✅ PERFORMANCE: Doubled for peak load
    inboundExecutor.setQueueCapacity(1000); // ✅ PERFORMANCE: Doubled queue capacity
    inboundExecutor.setThreadNamePrefix("ws-inbound-");
    inboundExecutor.setKeepAliveSeconds(120); // ✅ PERFORMANCE: Keep threads alive longer
    inboundExecutor.setAllowCoreThreadTimeOut(false); // Keep core threads always active
    inboundExecutor.initialize();

    registration.interceptors(webSocketAuthInterceptor).taskExecutor(inboundExecutor);
  }

  @Override
  public void configureClientOutboundChannel(ChannelRegistration registration) {
    // ✅ PERFORMANCE: Ultra-optimized ThreadPoolTaskExecutor for outbound messages
    ThreadPoolTaskExecutor outboundExecutor = new ThreadPoolTaskExecutor();
    outboundExecutor.setCorePoolSize(16); // ✅ PERFORMANCE: Doubled for ultra-fast processing
    outboundExecutor.setMaxPoolSize(32); // ✅ PERFORMANCE: Doubled for peak load
    outboundExecutor.setQueueCapacity(1000); // ✅ PERFORMANCE: Doubled queue capacity
    outboundExecutor.setThreadNamePrefix("ws-outbound-");
    outboundExecutor.setKeepAliveSeconds(120); // ✅ PERFORMANCE: Keep threads alive longer
    outboundExecutor.setAllowCoreThreadTimeOut(false); // Keep core threads always active
    outboundExecutor.initialize();

    registration.taskExecutor(outboundExecutor);
  }

  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
    registration
        .setMessageSizeLimit(128 * 1024) // ✅ PERFORMANCE: 128KB - Doubled for larger messages
        .setSendBufferSizeLimit(2 * 1024 * 1024) // ✅ PERFORMANCE: 2MB - Doubled buffer size
        .setSendTimeLimit(5000) // ✅ PERFORMANCE: 5 seconds - Ultra-fast response
        .setTimeToFirstMessage(8000); // ✅ PERFORMANCE: 8 seconds - Ultra-fast connection
  }

  // WebSocket security is now handled by WebSocketSecurityConfig
  // This prevents conflicts with the separate security configuration
}
