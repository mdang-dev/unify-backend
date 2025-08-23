package com.unify.app.media.domain;

import com.unify.app.media.domain.models.*;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.UserMapper;
import com.unify.app.users.domain.UserService;
import com.unify.app.users.domain.models.UserDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamService {

  private final StreamRepository streamRepository;
  private final LivekitService liveKitService;
  private final IngressService ingressService;
  private final UserService userService;
  private final StreamMapper mapper;
  private final UserMapper userMapper;

  public StreamDto createStream(String title, String description, String streamerId, String type) {
    String roomId = UUID.randomUUID().toString();

    User user = userService.findUserById(streamerId);

    Map<String, String> ingressCredentials =
        ingressService.generateIngressCredentials(roomId, type, user.getId());
    Stream room =
        Stream.builder()
            .title(title)
            .roomId(roomId)
            .description(description)
            .user(user)
            .serverUrl(ingressCredentials.get("url"))
            .streamKey(ingressCredentials.get("key"))
            .build();

    liveKitService.createRoom(roomId);
    return mapper.toDto(streamRepository.save(room));
  }

  public void createInitStream(String name, User user) {
    Stream stream = Stream.builder().user(user).name(name).build();
    streamRepository.save(stream);
  }

  public void createIngress(CreateIngressRequest req) {
    Map<String, String> ingressInfo = ingressService.createIngress(req);
    Stream stream =
        streamRepository
            .findByUserId(req.participantIdentity())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Stream not found with user id " + req.participantIdentity()));
    stream.setIngressId(ingressInfo.get("ingressId"));
    stream.setServerUrl(ingressInfo.get("url"));
    stream.setStreamKey(ingressInfo.get("key"));
    streamRepository.save(stream);
  }

  public ConnectionResponse getConnection(String userId) {
    Stream stream =
        streamRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new StreamNotFoundException("Stream not found with user id " + userId));
    return new ConnectionResponse(stream.getServerUrl(), stream.getStreamKey());
  }

  public String generateViewerToken(String hostIdentity, String selfIdentity) {

    var host = userService.findUserById(hostIdentity);

    var self = userService.findUserById(selfIdentity);

    boolean isHost = self.getId().equals(host.getId());

    return liveKitService.generateAccessToken(
        host.getId(),
        isHost ? "host-" + self.getId() : self.getId(),
        self.getUsername(),
        self.getFirstName() + " " + self.getLastName(),
        self.latestAvatar().getUrl());
  }

  public StreamDto startStream(String roomId) {
    Stream room =
        streamRepository
            .findById(roomId)
            .orElseThrow(() -> new StreamNotFoundException("Stream room not found"));

    room.setIsLive(true);
    return mapper.toDto(streamRepository.save(room));
  }

  public StreamDto endStream(String roomId) {
    Stream room =
        streamRepository
            .findById(roomId)
            .orElseThrow(() -> new StreamNotFoundException("Stream room not found"));

    room.setIsLive(false);
    room.setEndTime(LocalDateTime.now());
    liveKitService.deleteRoom(roomId);
    return mapper.toDto(streamRepository.save(room));
  }

  public List<StreamDto> getLiveStreams() {
    return streamRepository.findByIsLive(true).stream().map(mapper::toDto).toList();
  }

  public StreamDto getStreamByRoomId(String roomId) {
    Stream room =
        streamRepository
            .findByRoomId(roomId)
            .orElseThrow(() -> new StreamNotFoundException("Stream not found"));
    return mapper.toDto(room);
  }

  public void updateIsLiveStream(String ingressId, boolean isLive) {
    streamRepository
        .findByIngressId(ingressId)
        .ifPresentOrElse(
            stream -> {
              stream.setIsLive(isLive);
              streamRepository.save(stream);
            },
            () -> log.warn("Stream with ingressId={} not found", ingressId));
  }

  public List<UserDto> findStreamsFollowedBy(@RequestParam String currentUserId) {
    return streamRepository.findLiveUsersFollowedBy(currentUserId).stream()
        .map(userMapper::toUserDTO)
        .collect(Collectors.toList());
  }

    public void updateTitleAndThumbnail(String userId, StreamUpdateDto request) {
        Stream stream = streamRepository.findByUserId(userId)
                .orElseThrow(() -> new StreamNotFoundException("Stream not found for userId: " + userId));

        if (request.title() != null) {
            stream.setTitle(request.title());
        }
        if (request.thumbnailUrl() != null) {
            stream.setThumbnailUrl(request.thumbnailUrl());
        }

        streamRepository.save(stream);
    }

    public StreamChatSettingsDto getSettings(String userId) {
        Stream stream = streamRepository.findByUserId(userId)
                .orElseThrow(() -> new StreamNotFoundException("Stream not found for userId: " + userId));
        return  new StreamChatSettingsDto(stream.getIsChatEnabled(), stream.getIsChatDelayed(), stream.getIsChatFollowersOnly());
    }

    public void updateChatSettings(String userId, StreamChatSettingsDto request) {
        Stream stream = streamRepository.findByUserId(userId)
                .orElseThrow(() -> new StreamNotFoundException("Stream not found for userId: " + userId));

        if (request.isChatEnabled() != null) {
            stream.setIsChatEnabled(request.isChatEnabled());
        }
        if (request.isChatDelayed() != null) {
            stream.setIsChatDelayed(request.isChatDelayed());
        }
        if (request.isChatFollowersOnly() != null) {
            stream.setIsChatFollowersOnly(request.isChatFollowersOnly());
        }

        streamRepository.save(stream);
    }

    public  boolean getLiveStatus(String userId){
        Stream stream = streamRepository.findByUserId(userId)
                .orElseThrow(() -> new StreamNotFoundException("Stream not found for userId: " + userId));
        return  stream.getIsLive();
    }

    public String getUserIdByIngressId(String ingressId) {
        Stream stream = streamRepository.findByIngressId(ingressId)
                .orElseThrow(() -> new StreamNotFoundException("Stream not found for ingressId: " + ingressId));
        return stream.getUser().getId();
    }

    public StreamUpdateDto getStreamInfo(String userId) {
        Stream stream = streamRepository.findByUserId(userId)
                .orElseThrow(() -> new StreamNotFoundException("Stream not found for userId: " + userId));
        return  new StreamUpdateDto(stream.getTitle(), stream.getThumbnailUrl());
    }

}
