package com.unify.app.media.domain;

import com.unify.app.media.domain.models.CreateIngressRequest;
import io.livekit.server.IngressServiceClient;
import io.livekit.server.RoomServiceClient;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import livekit.LivekitIngress;
import livekit.LivekitIngress.IngressInfo;
import livekit.LivekitModels.Room;
import livekit.LivekitModels.TrackSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngressService {

  private final IngressServiceClient ingressClient;
  private final RoomServiceClient roomClient;

  public Map<String, String> generateIngressCredentials(
      String roomId, String type, String streamerId) {

    resetIngress(streamerId);

    var inputType =
        switch (type.toUpperCase()) {
          case "RTMP" -> LivekitIngress.IngressInput.RTMP_INPUT;
          case "WHIP" -> LivekitIngress.IngressInput.WHIP_INPUT;
          default -> throw new IllegalArgumentException("Unsupported input type: " + type);
        };

    var videoOptions =
        LivekitIngress.IngressVideoOptions.newBuilder()
            .setSource(TrackSource.CAMERA)
            .setPreset(LivekitIngress.IngressVideoEncodingPreset.H264_1080P_30FPS_3_LAYERS)
            .build();

    var isWhipInput = inputType.equals(LivekitIngress.IngressInput.WHIP_INPUT);

    try {
      var response =
          ingressClient
              .createIngress(
                  "ingress-" + roomId,
                  roomId,
                  "streamer-id",
                  "Camera User",
                  inputType,
                  null, // audioOptions (default)
                  isWhipInput ? videoOptions : null, // videoOptions
                  isWhipInput, // bypassTranscoding
                  true // enableTranscoding
                  )
              .execute();

      if (!response.isSuccessful() || response.body() == null)
        throw new IllegalStateException("Ingress creation failed: " + response.code());

      var ingress = response.body();
      return Map.of(
          "url", ingress.getUrl(),
          "key", ingress.getStreamKey());

    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create ingress", e);
    }
  }

  public Map<String, String> createIngress(CreateIngressRequest req) {
    resetIngress(req.participantIdentity());
    var inputType =
        switch (req.inputType().toUpperCase()) {
          case "RTMP" -> LivekitIngress.IngressInput.RTMP_INPUT;
          case "WHIP" -> LivekitIngress.IngressInput.WHIP_INPUT;
          default -> throw new IllegalArgumentException(
              "Unsupported input type: " + req.inputType());
        };

    var videoOptions =
        LivekitIngress.IngressVideoOptions.newBuilder()
            .setSource(TrackSource.CAMERA)
            .setPreset(LivekitIngress.IngressVideoEncodingPreset.H264_1080P_30FPS_3_LAYERS)
            .build();

    var audioOptions =
        LivekitIngress.IngressAudioOptions.newBuilder()
            .setSource(TrackSource.MICROPHONE)
            .setPreset(LivekitIngress.IngressAudioEncodingPreset.OPUS_STEREO_96KBPS)
            .build();

    var isWhipInput = inputType.equals(LivekitIngress.IngressInput.WHIP_INPUT);

    try {
      var response =
          ingressClient
              .createIngress(
                  req.participantName(),
                  req.participantIdentity(),
                  req.participantIdentity(),
                  req.participantName(),
                  inputType,
                  isWhipInput ? audioOptions : null, // audioOptions (default)
                  isWhipInput ? videoOptions : null, // videoOptions
                  isWhipInput, // bypassTranscoding
                  true // enableTranscoding
                  )
              .execute();

      if (!response.isSuccessful() || response.body() == null)
        throw new IllegalStateException("Ingress creation failed: " + response.code());

      var ingress = response.body();
      log.info("Ingress Url {} ==============================================", ingress.getUrl());
      return Map.of(
          "ingressId", ingress.getIngressId(),
          "url", ingress.getUrl(),
          "key", ingress.getStreamKey());

    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create ingress", e);
    }
  }

  private void resetIngress(String hostIdentity) {

    try {

      var callIngresses = ingressClient.listIngress(hostIdentity).execute();

      if (callIngresses.isSuccessful() && callIngresses.body() != null) {
        var ingresses = callIngresses.body();
        for (IngressInfo ingress : ingresses) {
          ingressClient.deleteIngress(ingress.getIngressId()).execute();
        }
      }

      var callRooms = roomClient.listRooms(List.of(hostIdentity)).execute();

      if (callRooms.isSuccessful() && callRooms.body() != null) {
        var rooms = callRooms.body();
        for (Room room : rooms) {
          roomClient.deleteRoom(room.getName()).execute();
        }
      }

    } catch (IOException e) {

      throw new UncheckedIOException("Failed to reset ingress", e);
    }
  }
}
