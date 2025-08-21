// package com.unify.app.ws;
//
// import org.springframework.context.annotation.Configuration;
// import
// org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
// import
// org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
//
// @Configuration
// public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
//
//  @Override
//  protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//    messages
//        .nullDestMatcher()
//        .permitAll()
//        .simpSubscribeDestMatchers("/topic/**")
//        .permitAll()
//        .simpSubscribeDestMatchers("/queue/**")
//        .authenticated()
//        .simpSubscribeDestMatchers("/user/**")
//        .authenticated()
//        .simpDestMatchers("/app/**")
//        .authenticated()
//        .anyMessage()
//        .denyAll();
//  }
//
//  @Override
//  protected boolean sameOriginDisabled() {
//    // Disable same origin check for WebSocket connections
//    // This is needed because WebSocket connections don't follow the same origin policy
//    return true;
//  }
// }
