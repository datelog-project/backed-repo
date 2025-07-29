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
import me.jinheum.datelog.repository.UserConnectionRepository;
import me.jinheum.datelog.security.ConnectionValidator;

@Service
@RequiredArgsConstructor
@Transactional
public class UserConnectionService {

    private final UserConnectionRepository userConnectionRepository;
    private final UserAccountService userAccountService;
    private final ConnectionValidator connectionValidator;

    public UUID invitePartner(UserAccount user, UserAccount partner) {
        connectionValidator.validateInvite(user, partner);

        Optional<UserConnection> rejected = userConnectionRepository.findByUserAndPartner(partner, user)
            .filter(conn -> conn.getStatus() == ConnectionStatus.REJECTED);
        if (rejected.isPresent()) {
            UserConnection connection = rejected.get();

            connection.setUser(user);
            connection.setPartner(partner);
            connection.setStatus(ConnectionStatus.PENDING);
            connection.setStartedAt(LocalDateTime.now());

            userConnectionRepository.save(connection);
            return connection.getId();
        }
        
        UserConnection connection = UserConnection.builder()
                .user(user)
                .partner(partner)
                .status(ConnectionStatus.PENDING)
                .build();

        userConnectionRepository.save(connection);
        return connection.getId();
    }

    public void acceptInvite(UUID connectionId, UUID currentUserId) {
        UserConnection connection = connectionValidator.validatePartnerInvitation(connectionId, currentUserId);
        connection.setStatus(ConnectionStatus.CONNECTED);
        connection.setStartedAt(LocalDateTime.now());
        userConnectionRepository.save(connection);
    }

    public void rejectInvite(UUID connectionId, UUID currentUserId) {
        UserConnection connection = connectionValidator.validatePartnerInvitation(connectionId, currentUserId);
        connection.setStatus(ConnectionStatus.REJECTED);
        userConnectionRepository.save(connection);
    }

    public void endConnection(UUID connectionId, UUID currentUserId) {
        UserConnection connection = connectionValidator.validateCanEndConnection(connectionId, currentUserId);
        connection.setStatus(ConnectionStatus.ENDED);
        connection.setStartedAt(null);
        userConnectionRepository.save(connection);
    }

    public UUID reconnectByEmail(UUID userId, String partnerEmail) {
        UserAccount user = userAccountService.getUserById(userId);
        UserAccount partner = userAccountService.getUserByEmail(partnerEmail);

        UserConnection connection = connectionValidator.getEndedConnectionBetween(user, partner);

        connection.setUser(user);
        connection.setPartner(partner);
        connection.setStatus(ConnectionStatus.PENDING);
        connection.setStartedAt(LocalDateTime.now());
        userConnectionRepository.save(connection);
        
        return connection.getId();
    }

    public boolean existsPreviousConnection(UserAccount user, String partnerEmail) {
        UserAccount partner = userAccountService.getUserByEmail(partnerEmail);
        // 과거 연결 기록 있는지 여부 조회
        Optional<UserConnection> connections = userConnectionRepository.findByUserAndPartner(user, partner);
        return !connections.isEmpty();
    }
}