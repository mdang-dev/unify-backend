package com.unify.app.media.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unify.app.ApplicationProperties;
import com.unify.app.users.domain.UserService;
import com.unify.app.users.domain.models.UserDto;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenGenerator {

  private final ApplicationProperties properties;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  public String createToken(String userId, String room) throws JsonProcessingException {
    UserDto user = userService.findById(userId);
    if (user != null) {
      AccessToken token =
          new AccessToken(properties.livekitApiKey(), properties.livekitApiSecret());
      token.setIdentity(userId);
      token.setName(user.firstName() + " " + user.lastName());

      Map<String, String> metadataMap = new HashMap<>();
      metadataMap.put("avatar", user.avatar() != null ? user.avatar().url() : null);

      String metadataJson = objectMapper.writeValueAsString(metadataMap);
      token.setMetadata(metadataJson);
      token.addGrants(new RoomJoin(true), new RoomName(room));

      return token.toJwt();
    }
    return null;
  }
}
