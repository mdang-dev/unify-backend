package com.unify.app.media.web.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.unify.app.media.domain.TokenGenerator;
import com.unify.app.media.domain.models.AcceptCallDto;
import com.unify.app.media.domain.models.CallActionResponse;
import com.unify.app.media.domain.models.CallNotification;
import com.unify.app.media.domain.models.CallRequest;
import com.unify.app.media.domain.models.CallResponse;
import com.unify.app.media.domain.models.CallSession;
import com.unify.app.media.domain.models.CallTokenResponse;
import com.unify.app.media.domain.models.RejectCallDto;
import com.unify.app.users.domain.UserService;
import com.unify.app.users.domain.models.UserDto;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/call")
@RequiredArgsConstructor
@Slf4j
class CallController {

  private final SimpMessagingTemplate messagingTemplate;
  private final TokenGenerator tokenGenerator;
  private final UserService userService;

  private Map<String, CallSession> activeCalls = new HashMap<>();

  @PostMapping
  public ResponseEntity<CallResponse> startCall(@Valid @RequestBody CallRequest request)
      throws JsonProcessingException {

    String room = "" + UUID.randomUUID();
    String callerId = request.callerId();
    String calleeId = request.calleeId();
    boolean isVideo = request.video();
    String callerToken = tokenGenerator.createToken(callerId, room);
    String calleeToken = tokenGenerator.createToken(calleeId, room);

    UserDto caller = userService.findById(callerId);
    UserDto callee = userService.findById(calleeId);

    // Create caller session
    CallSession callerSession = CallSession.builder()
        .token(callerToken)
        .video(isVideo)
        .isCaller(true)
        .calleeName(callee.firstName() + " " + callee.lastName())
        .calleeAvatar(callee.avatar().url())
        .room(room)
        .userId(callerId)
        .build();

    // Create callee session
    CallSession calleeSession = CallSession.builder()
        .token(calleeToken)
        .video(isVideo)
        .isCaller(false)
        .room(room)
        .userId(calleeId)
        .build();

    activeCalls.put(room + "-" + callerId, callerSession);
    activeCalls.put(room + "-" + calleeId, calleeSession);

    // Create call notification
    CallNotification notification = new CallNotification(room, callerId, caller.firstName() + " " + caller.lastName());

    messagingTemplate.convertAndSend("/topic/call/" + calleeId, notification);

    log.info("{} calling {}", callerId, calleeId);

    CallResponse response = new CallResponse(room);

    return ResponseEntity.ok(response);
  }

  @MessageMapping("/call.accept")
  public void acceptedCall(@Payload AcceptCallDto acceptCallDto) {
    log.info(
        "User {} accepted call from {}", acceptCallDto.fromUser(), acceptCallDto.acceptedFrom());

    CallActionResponse response = new CallActionResponse("accept", "Call accepted");

    messagingTemplate.convertAndSend("/topic/call/" + acceptCallDto.acceptedFrom(), response);
  }

  @MessageMapping("/call.reject")
  public void rejectCall(@Payload RejectCallDto rejectDto) {
    String room = rejectDto.room();
    String callerId = rejectDto.callerId();
    String calleeId = rejectDto.calleeId();
    String callerKey = room + "-" + callerId;
    String calleeKey = room + "-" + calleeId;

    CallSession activeCallerCall = activeCalls.get(callerKey);
    CallSession activeCalleeCall = activeCalls.get(calleeKey);

    if (activeCallerCall != null)
      activeCalls.remove(callerKey);
    if (activeCalleeCall != null)
      activeCalls.remove(calleeKey);

    log.info("User {} reject call from {}", calleeId, callerId);

    CallActionResponse response = new CallActionResponse("reject", "Call rejected");

    messagingTemplate.convertAndSend("/topic/call/" + rejectDto.callerId(), response);
  }

  @DeleteMapping("/{code}")
  public ResponseEntity<Void> endCall(@PathVariable String code) {
    if (!activeCalls.containsKey(code)) {
      return ResponseEntity.notFound().build();
    }
    activeCalls.remove(code);
    log.info("Ending call with code: {}", code);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/token/{code}")
  public ResponseEntity<CallTokenResponse> getToken(@PathVariable String code)
      throws JsonProcessingException {

    CallSession session = activeCalls.get(code);
    if (session == null) {
      return ResponseEntity.notFound().build();
    }

    CallTokenResponse tokenResponse = new CallTokenResponse(
        session.getToken(),
        session.isVideo(),
        session.isCaller(),
        session.getCalleeName(),
        session.getCalleeAvatar());

    return ResponseEntity.ok(tokenResponse);
  }
}
