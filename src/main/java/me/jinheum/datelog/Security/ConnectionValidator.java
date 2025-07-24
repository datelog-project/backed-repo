package me.jinheum.datelog.security;

import java.util.List;
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

    public void validateInvite(UserAccount user, UserAccount partner) { //초대할 때 검증
        if (user.getId().equals(partner.getId())) {
            throw new IllegalArgumentException("본인에게 초대할 수 없습니다.");
        } //ok

        if (hasPendingInviteFrom(user, partner)) {
            throw new IllegalStateException("상대방이 이미 나를 초대했습니다. 받은 초대를 수락해주세요.");
        } //ok

        if (hasAnyActiveConnection(user, partner)) {
            throw new IllegalStateException("이미 초대를 보냈거나, 이미 연결된 유저가 있습니다.");
        } //ok
    }

    public boolean hasPendingInviteFrom(UserAccount user, UserAccount partner) { //초대 보냈는지 확인 
        return userConnectionRepository.findByUserAndPartner(user, partner)
                .filter(conn -> 
                        conn.getStatus() == ConnectionStatus.PENDING &&
                        conn.getUser().getId().equals(partner.getId())
                        )
                .isPresent();
    }

    public boolean hasAnyActiveConnection(UserAccount user, UserAccount partner) {
        return userConnectionRepository.existsPendingConnectionForUsers(
            List.of(user, partner),
            List.of(ConnectionStatus.PENDING, ConnectionStatus.CONNECTED));
    }

    private UserConnection getConnectionOrThrow(UUID connectionId) { //연결 조회하기 없으면 예외
        return userConnectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("연결이 존재하지 않습니다."));
    } //ok

    public UserConnection validatePartnerInvitation(UUID connectionId, UUID currentUserId) { //현재 사용자(partner임 왜냐하면 초대 받은 사람이 partner쪽이니까)가 수락 거절할 권한 있는지
        UserConnection connection =getConnectionOrThrow(connectionId);

        if (!connection.getPartner().getId().equals(currentUserId)) {
            throw new IllegalStateException("초대를 받은 사람만 수락/거절할 수 있습니다.");
        } //ok

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 초대입니다.");
        }

        return connection;
    }

    public UserConnection validateCanEndConnection(UUID connectionId, UUID currentUserId) { //현재 사용자가 연결을 ENDED할 권한 있는지
        UserConnection connection = getConnectionOrThrow(connectionId);

        if (!connection.getPartner().getId().equals(currentUserId) && !connection.getUser().getId().equals(currentUserId)) {
            throw new IllegalStateException("본인 연결만 종료할 수 있습니다.");
        }

        return connection;
    }

    public UserConnection getEndedConnectionBetween(UserAccount connectionUser, UserAccount currentUser) { // 두 사용자 사이에 ENDED 된 걸 찾아 재결합 할 수 있는지(만약 헤어지고 다른사람과 연결했다면 재결합 불가)
        UserConnection connection = userConnectionRepository
            .findByUserAndPartner(connectionUser, currentUser)
            .or(() -> userConnectionRepository.findByUserAndPartner(currentUser, connectionUser))
            .orElseThrow(() -> new IllegalArgumentException("연결이 존재하지 않습니다."));

        if (!connection.getUser().getId().equals(currentUser.getId()) && !connection.getPartner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("재결합은 연결 당사자만 요청할 수 있습니다.");
        } //ok

        if (hasAnyActiveConnection(connectionUser, connectionUser)) {
            throw new IllegalStateException("이미 연결된 유저가 있어 재결합할 수 없습니다.");
        } //ok

        if (connection.getStatus() != ConnectionStatus.ENDED && connection.getStatus() != ConnectionStatus.REJECTED) {
            throw new IllegalArgumentException("재결합 가능한 연결이 없습니다.");
        }

        return connection;
    }
}