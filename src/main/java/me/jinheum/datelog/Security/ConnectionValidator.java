package me.jinheum.datelog.security;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.entity.UserConnection;
import me.jinheum.datelog.entity.enums.ConnectionStatus;
import me.jinheum.datelog.repository.UserConnectionRepository;

@Component
@RequiredArgsConstructor
public class ConnectionValidator {

    private final UserConnectionRepository userConnectionRepository;

    public void validateInvite(UserAccount inviter, UserAccount partner) {
        if (inviter.getEmail().equals(partner.getEmail())) {
            throw new IllegalArgumentException("본인에게 초대할 수 없습니다.");
        }

        if (hasPendingInviteFrom(partner, inviter)) {
            throw new IllegalStateException("상대방이 이미 나를 초대했습니다. 받은 초대를 수락해주세요.");
        }

        if (hasAnyActiveConnection(inviter)) {
            throw new IllegalStateException("이미 연결된 유저가 있습니다.");
        }
    }

    public boolean hasPendingInviteFrom(UserAccount user, UserAccount partner) {
        return userConnectionRepository.findByUserAndPartner(user, partner)
                .filter(conn -> conn.getStatus() == ConnectionStatus.PENDING)
                .isPresent();
    }

    public boolean hasAnyActiveConnection(UserAccount user) {
        return userConnectionRepository.existsByUserOrPartner(user, user);
    }

    private UserConnection getConnectionOrThrow(UUID connectionId) {
        return userConnectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("초대가 존재하지 않습니다."));
    }

    public UserConnection validatePartnerInvitation(UUID connectionId, UUID currentUserId) {
        UserConnection connection =getConnectionOrThrow(connectionId);

        if (!connection.getUser().getId().equals(currentUserId) &&
            !connection.getPartner().getId().equals(currentUserId)) {
            throw new IllegalStateException("본인의 초대만 수락/거절할 수 있습니다.");
        }

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 초대입니다.");
        }

        return connection;
    }

    public UserConnection validateOwnedConnection(UUID connectionId, UUID currentUserId) {
        UserConnection connection = getConnectionOrThrow(connectionId);

        if (!connection.getPartner().getId().equals(currentUserId)) {
            throw new IllegalStateException("본인 연결만 종료할 수 있습니다.");
        }

        return connection;
    }

    public UserConnection getEndedConnectionBetween(UserAccount user, UserAccount partner) {
        return userConnectionRepository.findByUserAndPartner(user, partner)
                .filter(conn -> conn.getStatus() == ConnectionStatus.ENDED)
                .orElseThrow(() -> new IllegalArgumentException("재결합 가능한 연결이 없습니다."));
    }
}