package me.jinheum.datelog.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.WithLogRequest;
import me.jinheum.datelog.entity.UserConnection;
import me.jinheum.datelog.entity.WithLog;
import me.jinheum.datelog.exception.InvalidTokenException;
import me.jinheum.datelog.repository.UserConnectionRepository;
import me.jinheum.datelog.repository.WithLogRepository;

@Service
@RequiredArgsConstructor
public class WithLogService {

    private final WithLogRepository withLogRepository;
    private final UserConnectionRepository userConnectionRepository;

    @Transactional
    public void createWithLog(UUID ConnectionId,WithLogRequest request, UUID user) {
        UserConnection Connection = userConnectionRepository.findById(ConnectionId)
            .orElseThrow(() -> new IllegalArgumentException("해당 연결이 존재하지 않습니다."));

        UUID connectionUserId = user;

        boolean isAuthorized = Connection.getUser().getId().equals(connectionUserId)
            || Connection.getPartner().getId().equals(connectionUserId);

        if (!isAuthorized) {
            throw new InvalidTokenException("접근 권한이 없습니다.");
        }
        WithLog withLog = WithLog.builder()
            .userConnection(Connection)
            .date(request.date())
            .placeName(request.placeName())
            .feelingScore(request.feelingScore())
            .note(request.note())
            .build();

        withLogRepository.save(withLog);
    }
}