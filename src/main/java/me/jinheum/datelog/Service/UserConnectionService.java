package me.jinheum.datelog.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.entity.UserConnection;
import me.jinheum.datelog.entity.enums.ConnectionStatus;
import me.jinheum.datelog.repository.UserAccountRepository;
import me.jinheum.datelog.repository.UserConnectionRepository;
import me.jinheum.datelog.security.ConnectionValidator;

@Service
@RequiredArgsConstructor
@Transactional
public class UserConnectionService {

    private final UserConnectionRepository userConnectionRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;
    private final ConnectionValidator connectionValidator;
    
    public void invitePartner(UUID inviterId, String partnerEmail) {
        UserAccount inviter = userAccountService.getUserById(inviterId);
        UserAccount partner = userAccountService.getUserByEmail(partnerEmail);

        connectionValidator.validateInvite(inviter, partner);

        UserConnection connection = UserConnection.builder()
                .user(inviter)
                .partner(partner)
                .status(ConnectionStatus.PENDING)
                .build();

        userConnectionRepository.save(connection);
    }

    public void acceptInvite(UUID connectionId, UUID currentUserId) {
        UserConnection connection = userConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("초대가 존재하지 않습니다."));

        if (!connection.getPartner().getId().equals(currentUserId)) {
            throw new IllegalStateException("본인의 초대만 수락할 수 있습니다.");
        }

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 초대입니다.");
        }

        connection.setStatus(ConnectionStatus.CONNECTED);
        connection.setStartedAt(LocalDateTime.now());

        userConnectionRepository.save(connection);
    }
    
    public void rejectInvite(UUID connectionId, UUID currentUserId) {
        UserConnection connection = userConnectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("초대가 존재하지 않습니다."));

        if (!connection.getPartner().getId().equals(currentUserId)) {
            throw new IllegalStateException("본인의 초대만 거절할 수 있습니다.");
        }

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("처리할 수 없는 상태의 초대입니다.");
        }

        userConnectionRepository.delete(connection);
    }

    public void endConnection(UUID connectionId, UUID currentUserId) {
        UserConnection connection = userConnectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("연결이 존재하지 않습니다."));

        if (!connection.getUser().getId().equals(currentUserId) && !connection.getPartner().getId().equals(currentUserId)) {
            throw new IllegalStateException("본인 연결만 종료할 수 있습니다.");
        }

        connection.setStatus(ConnectionStatus.ENDED);
        connection.setStartedAt(null);
        userConnectionRepository.save(connection);
    }

    public void reconnectByEmail(UUID userId, String partnerEmail) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserAccount partner = userAccountRepository.findByEmail(partnerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Partner not found"));

        Optional<UserConnection> existingConnection = userConnectionRepository.findByUserAndPartner(user, partner);

        if (existingConnection.isPresent()) {
            UserConnection connection = existingConnection.get();
            if (connection.getStatus() == ConnectionStatus.ENDED) {
                connection.setStatus(ConnectionStatus.CONNECTED);
                connection.setStartedAt(LocalDateTime.now());
                userConnectionRepository.save(connection);
                return;
            }
            throw new IllegalStateException("현재 연결 상태로 재결합 불가합니다.");
        } else {
            throw new IllegalArgumentException("존재하지 않는 연결입니다.");
        }
    }

}