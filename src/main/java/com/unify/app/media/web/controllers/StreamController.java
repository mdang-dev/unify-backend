package com.unify.app.media.web.controllers;

import com.unify.app.media.domain.StreamService;
import com.unify.app.media.domain.models.ConnectionResponse;
import com.unify.app.media.domain.models.CreateIngressRequest;
import com.unify.app.media.domain.models.CreateStreamRequest;
import com.unify.app.media.domain.models.StreamDto;
import com.unify.app.media.domain.models.ViewerTokenRequest;
import com.unify.app.users.domain.models.TokenResponse;
import com.unify.app.users.domain.models.UserDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
