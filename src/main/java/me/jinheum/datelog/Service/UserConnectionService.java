package me.jinheum.datelog.service;

import java.time.LocalDateTime;
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
        UserConnection connection = connectionValidator.validatePartnerInvitation(connectionId, currentUserId);
        connection.setStatus(ConnectionStatus.CONNECTED);
        connection.setStartedAt(LocalDateTime.now());
        userConnectionRepository.save(connection);
    }

    public void rejectInvite(UUID connectionId, UUID currentUserId) {
        UserConnection connection = connectionValidator.validatePartnerInvitation(connectionId, currentUserId);
        userConnectionRepository.delete(connection);
    }

    public void endConnection(UUID connectionId, UUID currentUserId) {
        UserConnection connection = connectionValidator.validateOwnedConnection(connectionId, currentUserId);
        connection.setStatus(ConnectionStatus.ENDED);
        connection.setStartedAt(null);
        userConnectionRepository.save(connection);
    }

    public void reconnectByEmail(UUID userId, String partnerEmail) {
        UserAccount user = userAccountService.getUserById(userId);
        UserAccount partner = userAccountService.getUserByEmail(partnerEmail);

        UserConnection connection = connectionValidator.getEndedConnectionBetween(user, partner);

        connection.setStatus(ConnectionStatus.CONNECTED);
        connection.setStartedAt(LocalDateTime.now());
        userConnectionRepository.save(connection);
    }
}