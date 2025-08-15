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
            "http://localhost:3000", // Frontend development
            "http://localhost:3001", // Frontend alternative port
            "https://unify.qzz.io", // Production domain
            "https://*.unify.qzz.io", // Subdomains
            "https://unify.id.vn", // Production domain
            "https://*.unify.id.vn" // Subdomains
            )
        .withSockJS()
        .setHeartbeatTime(20000) // ✅ OPTIMIZED: Increased for better stability
        .setDisconnectDelay(10000) // ✅ OPTIMIZED: Increased for graceful disconnection
        .setHttpMessageCacheSize(200) // ✅ OPTIMIZED: Reduced to prevent memory leaks
        .setWebSocketEnabled(true)
        .setSessionCookieNeeded(false);
  }

  @Override
  public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue", "/user");
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");

    // ✅ OPTIMIZED: Add message size limits to prevent memory issues
    registry.setPreservePublishOrder(true);
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    // ✅ OPTIMIZED: Balanced ThreadPoolTaskExecutor for real-time chat
    ThreadPoolTaskExecutor inboundExecutor = new ThreadPoolTaskExecutor();
    inboundExecutor.setCorePoolSize(2); // ✅ OPTIMIZED: Reduced for better resource management
    inboundExecutor.setMaxPoolSize(4); // ✅ OPTIMIZED: Reduced for better resource management
    inboundExecutor.setQueueCapacity(50); // ✅ OPTIMIZED: Reduced to prevent memory buildup
    inboundExecutor.setThreadNamePrefix("ws-inbound-");
    inboundExecutor.setKeepAliveSeconds(30); // ✅ OPTIMIZED: Reduced for faster cleanup
    inboundExecutor.setAllowCoreThreadTimeOut(true); // ✅ OPTIMIZED: Allow core threads to timeout
    inboundExecutor.setWaitForTasksToCompleteOnShutdown(true); // ✅ OPTIMIZED: Graceful shutdown
    inboundExecutor.setAwaitTerminationSeconds(15); // ✅ OPTIMIZED: Faster shutdown
    inboundExecutor.setRejectedExecutionHandler(
        new ThreadPoolExecutor.CallerRunsPolicy()); // ✅ OPTIMIZED: Prevent task rejection
    inboundExecutor.initialize();

    registration.interceptors(webSocketAuthInterceptor).taskExecutor(inboundExecutor);
  }

  @Override
  public void configureClientOutboundChannel(ChannelRegistration registration) {
    // ✅ OPTIMIZED: Balanced ThreadPoolTaskExecutor for outbound messages
    ThreadPoolTaskExecutor outboundExecutor = new ThreadPoolTaskExecutor();
    outboundExecutor.setCorePoolSize(2); // ✅ OPTIMIZED: Reduced for better resource management
    outboundExecutor.setMaxPoolSize(4); // ✅ OPTIMIZED: Reduced for better resource management
    outboundExecutor.setQueueCapacity(50); // ✅ OPTIMIZED: Reduced to prevent memory buildup
    outboundExecutor.setThreadNamePrefix("ws-outbound-");
    outboundExecutor.setKeepAliveSeconds(30); // ✅ OPTIMIZED: Reduced for faster cleanup
    outboundExecutor.setAllowCoreThreadTimeOut(true); // ✅ OPTIMIZED: Allow core threads to timeout
    outboundExecutor.setWaitForTasksToCompleteOnShutdown(true); // ✅ OPTIMIZED: Graceful shutdown
    outboundExecutor.setAwaitTerminationSeconds(15); // ✅ OPTIMIZED: Faster shutdown
    outboundExecutor.setRejectedExecutionHandler(
        new ThreadPoolExecutor.CallerRunsPolicy()); // ✅ OPTIMIZED: Prevent task rejection
    outboundExecutor.initialize();

    registration.taskExecutor(outboundExecutor);
  }

  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
    registration
        .setMessageSizeLimit(32 * 1024) // ✅ OPTIMIZED: 32KB - Reduced for better performance
        .setSendBufferSizeLimit(512 * 1024) // ✅ OPTIMIZED: 512KB - Reduced for better performance
        .setSendTimeLimit(2000) // ✅ OPTIMIZED: 2 seconds - Faster response time
        .setTimeToFirstMessage(3000); // ✅ OPTIMIZED: 3 seconds - Faster connection time
  }

  // WebSocket security is now handled by WebSocketSecurityConfig
  // This prevents conflicts with the separate security configuration
}
