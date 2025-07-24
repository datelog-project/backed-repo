package me.jinheum.datelog.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.WithLogPreviewResponse;
import me.jinheum.datelog.dto.WithLogRequest;
import me.jinheum.datelog.dto.WithLogResponse;
import me.jinheum.datelog.entity.Media;
import me.jinheum.datelog.entity.UserConnection;
import me.jinheum.datelog.entity.WithLog;
import me.jinheum.datelog.repository.MediaRepository;
import me.jinheum.datelog.repository.UserConnectionRepository;
import me.jinheum.datelog.repository.WithLogRepository;
import me.jinheum.datelog.security.WithLogValidator;

@Service
@RequiredArgsConstructor
public class WithLogService {

    private final WithLogRepository withLogRepository;
    private final UserConnectionRepository userConnectionRepository;
    private final MediaRepository mediaRepository;
    private final WithLogValidator withLogValidator;
    private final FileStorageService fileStorageService;

    @Transactional
    public void createWithLog(UUID connectionId, WithLogRequest request, UUID userId) {
        UserConnection connection = userConnectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("해당 연결이 존재하지 않습니다."));

        withLogValidator.validateUserInConnection(connection, userId);

        WithLog withLog = WithLog.builder()
            .userConnection(connection)
            .date(request.date())
            .placeName(request.placeName())
            .placeAddress(request.placeAddress())
            .placeLat(request.placeLat())          
            .placeLng(request.placeLng())
            .feelingScore(request.feelingScore())
            .note(request.note())
            .cost(request.cost())
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
    public void deleteWithLog(UUID withLogId, UUID userId) {
        WithLog withLog = withLogRepository.findById(withLogId)
            .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        UserConnection connection = withLog.getUserConnection();

        withLogValidator.validateUserInConnection(connection, userId);

        withLogRepository.delete(withLog);
    }

    @Transactional(readOnly = true)
    public List<WithLogPreviewResponse> getWithLogPreviews(UUID connectionId, UUID userId) {
        UserConnection connection = userConnectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        withLogValidator.validateUserInConnection(connection, userId);

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
        withLogValidator.validateUserInConnection(connection, userId);

        return WithLogResponse.from(withLog);
    }

    @Transactional
    public WithLogResponse updateWithLog(UUID withLogId, WithLogRequest request, List<MultipartFile> images, UUID userId) {
        WithLog withLog = withLogRepository.findById(withLogId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        UserConnection connection = withLog.getUserConnection();
        withLogValidator.validateUserInConnection(connection, userId);

        withLog.setDate(request.date());
        withLog.setPlaceName(request.placeName());
        withLog.setPlaceAddress(request.placeAddress());
        withLog.setPlaceLat(request.placeLat());
        withLog.setPlaceLng(request.placeLng());
        withLog.setFeelingScore(request.feelingScore());
        withLog.setNote(request.note());
        withLog.setCost(request.cost());

        withLog.getMediaList().clear();

        // 기존 mediaList (URL로 전달된 것) 추가
        if (request.mediaList() != null) {
            List<Media> mediaEntities = request.mediaList().stream()
                    .map(mr -> Media.builder()
                            .withLog(withLog)
                            .mediaUrl(mr.mediaUrl())
                            .mediaType(mr.mediaType())
                            .build())
                    .toList();
            withLog.getMediaList().addAll(mediaEntities);
        }

        // MultipartFile 처리
        if (images != null && !images.isEmpty()) {
            List<Media> uploadedMedias = images.stream()
                    .map(file -> {
                        String uploadedUrl = fileStorageService.upload(file); // 이 부분은 구현 필요
                        return Media.builder()
                                .withLog(withLog)
                                .mediaUrl(uploadedUrl)
                                .mediaType("IMAGE") // 또는 자동 판별
                                .build();
                    })
                    .toList();
            withLog.getMediaList().addAll(uploadedMedias);
        }

        mediaRepository.saveAll(withLog.getMediaList());
        withLogRepository.save(withLog);

        return WithLogResponse.from(withLog);
    }

}