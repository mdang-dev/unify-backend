package com.unify.app.media.web.controllers;

import com.unify.app.media.domain.StreamService;
import com.unify.app.media.domain.models.*;
import com.unify.app.users.domain.models.TokenResponse;
import com.unify.app.users.domain.models.UserDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/streams")
@RequiredArgsConstructor
public class StreamController {

  private final StreamService streamService;

  @PostMapping("/create")
  public ResponseEntity<StreamDto> createStream(@RequestBody CreateStreamRequest request) {
    StreamDto dto =
        streamService.createStream(
            request.title(), request.description(), request.streamerId(), request.type());
    return ResponseEntity.ok(dto);
  }

  @PostMapping("/create-connection")
  public ResponseEntity<Void> createConnection(@RequestBody CreateIngressRequest req) {
    streamService.createIngress(req);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{userId}/connection")
  public ResponseEntity<ConnectionResponse> getConnection(@PathVariable String userId) {
    return ResponseEntity.ok(streamService.getConnection(userId));
  }

  @GetMapping("/following")
  public ResponseEntity<List<UserDto>> getFollowingStreams(@RequestParam String currentUserId) {
    return ResponseEntity.ok(streamService.findStreamsFollowedBy(currentUserId));
  }

  @PostMapping("/create-viewer-token")
  public ResponseEntity<TokenResponse> createViewerToken(@RequestBody ViewerTokenRequest request) {
    return ResponseEntity.ok(
        new TokenResponse(
            streamService.generateViewerToken(request.hostIdentity(), request.selfIdentity())));
  }

  @PostMapping("/{roomId}/start")
  public ResponseEntity<StreamDto> startStream(@PathVariable String roomId) {
    return ResponseEntity.ok(streamService.startStream(roomId));
  }

  @PostMapping("/{roomId}/end")
  public ResponseEntity<StreamDto> endStream(@PathVariable String roomId) {
    return ResponseEntity.ok(streamService.endStream(roomId));
  }

  @GetMapping("/live")
  public ResponseEntity<List<StreamDto>> getLiveStreams() {
    return ResponseEntity.ok(streamService.getLiveStreams());
  }

  @GetMapping("/{roomId}")
  public ResponseEntity<StreamDto> getStream(@PathVariable String roomId) {
    return ResponseEntity.ok(streamService.getStreamByRoomId(roomId));
  }

    @GetMapping("/user/{userId}/chat-settings")
    public ResponseEntity<StreamChatSettingsDto> updateChatSettings(
            @PathVariable String userId
    ) {
        return  ResponseEntity.ok(streamService.getSettings(userId));
    }

    @GetMapping("/user/{userId}/live-status")
    public ResponseEntity<Boolean> getLiveStatus(
            @PathVariable String userId
    ) {
        return  ResponseEntity.ok(streamService.getLiveStatus(userId));
    }

    @PutMapping("/user/{userId}/details")
    public ResponseEntity<Void> updateTitleAndThumbnail(
            @PathVariable String userId,
            @RequestBody StreamUpdateDto request
    ) {
        streamService.updateTitleAndThumbnail(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/get-details")
    public ResponseEntity<StreamUpdateDto> getTitleAndThumbnail(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(streamService.getStreamInfo(userId));
    }


    @PutMapping("/user/{userId}/chat-settings")
    public ResponseEntity<Void> updateChatSettings(
            @PathVariable String userId,
            @RequestBody StreamChatSettingsDto request
    ) {
        streamService.updateChatSettings(userId, request);
        return ResponseEntity.ok().build();
    }
}
