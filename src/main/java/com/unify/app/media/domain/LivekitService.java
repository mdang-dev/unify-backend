package com.unify.app.media.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unify.app.ApplicationProperties;
import io.livekit.server.AccessToken;
import io.livekit.server.CanPublish;
import io.livekit.server.CanPublishData;
import io.livekit.server.Room;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomServiceClient;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import livekit.LivekitModels;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

@Service
@RequiredArgsConstructor
public class LivekitService {

  private final RoomServiceClient roomClient;
  private final ApplicationProperties properties;
  private final ObjectMapper objectMapper;

  public String generateAccessToken(
      String room, String hostIdentity, String name, String fullName, String avatarUrl) {

    try {

      AccessToken token =
          new AccessToken(properties.livekitApiKey(), properties.livekitApiSecret());
      token.setIdentity(hostIdentity);
      token.setName(name);
      Map<String, String> metadata = new HashMap<>();
      metadata.put("fullName", fullName);
      metadata.put("avatar", avatarUrl);
      String metadataJson = objectMapper.writeValueAsString(metadata);
      token.setMetadata(metadataJson);
      token.addGrants(
          new RoomJoin(true), new Room(room), new CanPublish(true), new CanPublishData(true));

      return token.toJwt();

    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to generate access token", e);
    }
  }

  public LivekitModels.Room createRoom(String roomId) {
    try {
      Call<LivekitModels.Room> call =
          roomClient.createRoom(
              roomId,
              null, // emptyTimeout
              null, // maxParticipants
              null, // nodeId
              null, // metadata
              null, // minPlayoutDelay
              null, // maxPlayoutDelay
              null, // syncStreams
              null // departureTimeout
              );
      Response<LivekitModels.Room> response = call.execute();
      if (response.isSuccessful()) {
        return response.body();
      }
      return null;
    } catch (IOException e) {
      throw new RuntimeException("Failed to create LiveKit room", e);
    }
  }

  public void deleteRoom(String roomId) {
    try {
      Call<Void> call = roomClient.deleteRoom(roomId);
      Response<Void> response = call.execute();
      if (!response.isSuccessful()) {
        throw new RuntimeException(
            "Failed to delete room: " + response.code() + " - " + response.message());
      }

    } catch (IOException e) {
      throw new RuntimeException("Failed to delete LiveKit room", e);
    }
  }

  public List<LivekitModels.Room> listRooms() {
    try {
      Call<List<LivekitModels.Room>> call = roomClient.listRooms(Collections.emptyList());
      Response<List<LivekitModels.Room>> response = call.execute();
      if (response.isSuccessful()) {
        return response.body();
      }
      return null;

    } catch (IOException e) {
      throw new RuntimeException("Failed to list LiveKit rooms", e);
    }
  }
}
