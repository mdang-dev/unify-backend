package com.unify.app.ws;

import com.unify.app.security.JwtService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final JwtService jwtService;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .addInterceptors(new AuthHandshakeInterceptor())
        .withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue", "/user");
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
  }

  private class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
        @NonNull ServerHttpRequest request,
        @NonNull ServerHttpResponse response,
        @NonNull WebSocketHandler wsHandler,
        @NonNull Map<String, Object> attributes) {

      String token = extractToken(request);
      if (token == null || !jwtService.validToken(token)) {
        log.warn("WebSocket handshake rejected. Invalid or missing token.");
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
      }

      return true;
    }

    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Exception exception) {}

    private String extractToken(ServerHttpRequest request) {
      // Check header
      List<String> authHeaders = request.getHeaders().get("Authorization");
      if (authHeaders != null && !authHeaders.isEmpty()) {
        return authHeaders.get(0).replace("Bearer ", "");
      }

      // Check query param
      String query = request.getURI().getQuery();
      if (query != null) {
        for (String param : query.split("&")) {
          if (param.startsWith("token=")) return param.split("=")[1];
        }
      }

      return null;
    }
  }
}
