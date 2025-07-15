package me.jinheum.datelog.service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.WithLogResponse;
import me.jinheum.datelog.entity.SharedLink;
import me.jinheum.datelog.entity.WithLog;
import me.jinheum.datelog.exception.NotFoundException;
import me.jinheum.datelog.repository.SharedLinkRepository;
import me.jinheum.datelog.repository.WithLogRepository;
import me.jinheum.datelog.security.WithLogValidator;


@Service
@RequiredArgsConstructor
public class SharedLinkService {
    
    private final SharedLinkRepository sharedLinkRepository;
    private final WithLogRepository withLogRepository;
    private final WithLogValidator withLogValidator;

    @Transactional
    public UUID createSharedLink(UUID withLogId, UUID userId){
        
        WithLog withLog = withLogRepository.findById(withLogId)
            .orElseThrow(() -> new NotFoundException("해당 게시글이 존재하지 않습니다."));
        
        withLogValidator.validateUserInConnection(withLog.getUserConnection(), userId);

        Optional<SharedLink> existing = sharedLinkRepository.findByWithLog(withLog);
        if (existing.isPresent()) {
            return existing.get().getId();
        }
        
        SharedLink sharedLink = SharedLink.builder()
            .withLog(withLog)
            .build();

        sharedLinkRepository.save(sharedLink);
        return sharedLink.getId();
    }

    @Transactional(readOnly = true)
    public WithLogResponse getSharedLogView(UUID sharedLinkId) {
        SharedLink sharedLink = sharedLinkRepository.findById(sharedLinkId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 공유 링크입니다."));

        WithLog withLog = sharedLink.getWithLog();

        return WithLogResponse.from(withLog);
    }
}