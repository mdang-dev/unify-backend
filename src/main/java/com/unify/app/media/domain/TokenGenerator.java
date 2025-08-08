package com.unify.app.media.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unify.app.ApplicationProperties;
import com.unify.app.media.domain.models.TokenResponse;
import com.unify.app.media.domain.models.UserMetadata;
import com.unify.app.users.domain.UserService;
import com.unify.app.users.domain.models.UserDto;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenGenerator {
  private final ApplicationProperties properties;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  public String createToken(String userId, String room) throws JsonProcessingException {
    TokenResponse response = createTokenWithResponse(userId, room);
    return response.isSuccess() ? response.token() : null;
  }

  public TokenResponse createTokenWithResponse(String userId, String room) {
    try {
      UserDto user = userService.findById(userId);
      if (user == null) {
        log.warn("User not found: {}", userId);
        return TokenResponse.error("User not found");
      }

      AccessToken token =
          new AccessToken(properties.livekitApiKey(), properties.livekitApiSecret());

      token.setIdentity(userId);
      token.setName(user.firstName() + " " + user.lastName());

      // Sử dụng UserMetadata record thay vì Map
      UserMetadata metadata = new UserMetadata(user.avatar() != null ? user.avatar().url() : null);

      String metadataJson = objectMapper.writeValueAsString(metadata);
      token.setMetadata(metadataJson);
      token.addGrants(new RoomJoin(true), new RoomName(room));

      String jwtToken = token.toJwt();
      log.debug("Token created successfully for user: {} in room: {}", userId, room);

      return TokenResponse.success(jwtToken);

    } catch (JsonProcessingException e) {
      log.error("Error serializing metadata for user: {}", userId, e);
      return TokenResponse.error("Failed to serialize metadata");
    } catch (Exception e) {
      log.error("Error creating token for user: {} in room: {}", userId, room, e);
      return TokenResponse.error("Failed to create token");
    }
  }

  public String createTokenForUser(UserDto user, String room) {
    if (user == null) {
      log.warn("User is null when creating token for room: {}", room);
      return null;
    }

    try {
      AccessToken token =
          new AccessToken(properties.livekitApiKey(), properties.livekitApiSecret());

      token.setIdentity(user.id());
      token.setName(user.firstName() + " " + user.lastName());

      UserMetadata metadata = new UserMetadata(user.avatar() != null ? user.avatar().url() : null);

      String metadataJson = objectMapper.writeValueAsString(metadata);
      token.setMetadata(metadataJson);
      token.addGrants(new RoomJoin(true), new RoomName(room));

      return token.toJwt();

    } catch (Exception e) {
      log.error("Error creating token for user: {} in room: {}", user.id(), room, e);
      return null;
    }
  }
}
