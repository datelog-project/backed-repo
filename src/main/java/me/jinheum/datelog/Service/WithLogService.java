package me.jinheum.datelog.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.WithLogPreviewResponse;
import me.jinheum.datelog.dto.WithLogRequest;
import me.jinheum.datelog.dto.WithLogResponse;
import me.jinheum.datelog.entity.Media;
import me.jinheum.datelog.entity.UserConnection;
import me.jinheum.datelog.entity.WithLog;
import me.jinheum.datelog.exception.AccessDeniedException;
import me.jinheum.datelog.repository.MediaRepository;
import me.jinheum.datelog.repository.UserConnectionRepository;
import me.jinheum.datelog.repository.WithLogRepository;

@Service
@RequiredArgsConstructor
public class WithLogService {

    private final WithLogRepository withLogRepository;
    private final UserConnectionRepository userConnectionRepository;
    private final MediaRepository mediaRepository;

    @Transactional
    public void createWithLog(UUID ConnectionId, WithLogRequest request, UUID user) {
        UserConnection Connection = userConnectionRepository.findById(ConnectionId)
            .orElseThrow(() -> new IllegalArgumentException("해당 연결이 존재하지 않습니다."));

        if (!Connection.getUser().getId().equals(user) &&
            !Connection.getPartner().getId().equals(user)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }
        WithLog withLog = WithLog.builder()
            .userConnection(Connection)
            .date(request.date())
            .placeName(request.placeName())
            .placeAddress(request.placeAddress())
            .placeLat(request.placeLat())          
            .placeLng(request.placeLng())
            .feelingScore(request.feelingScore())
            .note(request.note())
            .build();

        withLogRepository.save(withLog);
        if (request.mediaList() != null) {
            List<Media> medias = request.mediaList().stream()
                .map(mr -> Media.builder()
                    .withLog(withLog)
                    .mediaUrl(mr.mediaUrl())
                    .mediaType(mr.mediaType())
                    .build())
                .toList();

            mediaRepository.saveAll(medias);
        }
    }

    @Transactional
    public void deleteWithLog(UUID withLogId, UUID user) {
        WithLog withLog = withLogRepository.findById(withLogId)
            .orElseThrow(() -> new IllegalArgumentException("해당 로그가 존재하지 않습니다."));

        UserConnection connection = withLog.getUserConnection();

        if (!connection.getUser().getId().equals(user) &&
            !connection.getPartner().getId().equals(user)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        withLogRepository.delete(withLog);
    }

    @Transactional(readOnly = true)
    public List<WithLogPreviewResponse> getWithLogPreviews(UUID connectionId, UUID userId) {
        UserConnection connection = userConnectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("해당 연결이 존재하지 않습니다."));

        if (!connection.getUser().getId().equals(userId) &&
            !connection.getPartner().getId().equals(userId)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        List<WithLog> logs = withLogRepository.findByUserConnectionOrderByDateDesc(connection);
        return logs.stream()
            .map(WithLogPreviewResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public WithLogResponse getWithLogDetail(UUID withLogId, UUID userId) {
        WithLog withLog = withLogRepository.findById(withLogId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        UserConnection connection = withLog.getUserConnection();
        if (!connection.getUser().getId().equals(userId) &&
            !connection.getPartner().getId().equals(userId)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        return WithLogResponse.from(withLog);
    }
}