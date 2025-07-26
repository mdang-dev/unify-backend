package com.unify.app.ws;

import com.unify.app.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JwtService jwt;
  private final UserDetailsService users;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) return message;

    var token = accessor.getFirstNativeHeader("token");
    if (token == null || !jwt.validToken(token)) return message;

    var user =
        users.loadUserByUsername(jwt.extractUsername(token.trim().replaceFirst("^Bearer ", "")));
    accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    return message;
  }
}
