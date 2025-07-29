package me.jinheum.datelog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.entity.UserConnection;
import me.jinheum.datelog.entity.enums.ConnectionStatus;
import me.jinheum.datelog.repository.UserAccountRepository;
import me.jinheum.datelog.repository.UserConnectionRepository;
import me.jinheum.datelog.security.ConnectionValidator;
import me.jinheum.datelog.service.UserAccountService;
import me.jinheum.datelog.service.UserConnectionService;

@ExtendWith(MockitoExtension.class)
class UserConnectionServiceTest {

    @Mock
    private UserConnectionRepository userConnectionRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private ConnectionValidator connectionValidator;

    @InjectMocks
    private UserConnectionService userConnectionService;

    @Test
    void 파트너_초대_성공() {
        UUID inviterId = UUID.randomUUID();
        String partnerEmail = "partner@test.com";

        UserAccount inviter = UserAccount.builder()
                .id(inviterId)
                .email("inviter@test.com")
                .build();

        UserAccount partner = UserAccount.builder()
                .id(UUID.randomUUID())
                .email(partnerEmail)
                .build();

        when(userAccountService.getUserById(inviterId)).thenReturn(inviter);
        when(userAccountService.getUserByEmail(partnerEmail)).thenReturn(partner);
        doNothing().when(connectionValidator).validateInvite(inviter, partner);
        when(userConnectionRepository.save(any(UserConnection.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        userConnectionService.invitePartner(inviter, partner);

        verify(userConnectionRepository, times(1)).save(any(UserConnection.class));
    }

    @Test
    void 초대_수락_성공() {
        UUID connectionId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        UserAccount partner = UserAccount.builder().id(currentUserId).build();
        UserConnection connection = UserConnection.builder()
                .id(connectionId)
                .partner(partner)
                .status(ConnectionStatus.PENDING)
                .build();

        when(userConnectionRepository.findById(connectionId)).thenReturn(Optional.of(connection));
        when(userConnectionRepository.save(any(UserConnection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userConnectionService.acceptInvite(connectionId, currentUserId);

        assertEquals(ConnectionStatus.CONNECTED, connection.getStatus());
        assertNotNull(connection.getStartedAt());
        verify(userConnectionRepository, times(1)).save(connection);
    }

    @Test
    void 초대_수락_실패_본인이_아님() {
        UUID connectionId = UUID.randomUUID();
        UUID wrongUserId = UUID.randomUUID();

        UserAccount partner = UserAccount.builder().id(UUID.randomUUID()).build();
        UserConnection connection = UserConnection.builder()
                .id(connectionId)
                .partner(partner)
                .status(ConnectionStatus.PENDING)
                .build();

        when(userConnectionRepository.findById(connectionId)).thenReturn(Optional.of(connection));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> userConnectionService.acceptInvite(connectionId, wrongUserId));

        assertEquals("본인의 초대만 수락할 수 있습니다.", ex.getMessage());
    }

    @Test
    void 초대_거절_성공() {
        UUID connectionId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        UserAccount partner = UserAccount.builder().id(currentUserId).build();
        UserConnection connection = UserConnection.builder()
                .id(connectionId)
                .partner(partner)
                .status(ConnectionStatus.PENDING)
                .build();

        when(userConnectionRepository.findById(connectionId)).thenReturn(Optional.of(connection));

        userConnectionService.rejectInvite(connectionId, currentUserId);

        verify(userConnectionRepository, times(1)).delete(connection);
    }

    @Test
    void 초대_거절_실패_본인이_아님() {
        UUID connectionId = UUID.randomUUID();
        UUID wrongUserId = UUID.randomUUID();

        UserAccount partner = UserAccount.builder().id(UUID.randomUUID()).build();
        UserConnection connection = UserConnection.builder()
                .id(connectionId)
                .partner(partner)
                .status(ConnectionStatus.PENDING)
                .build();

        when(userConnectionRepository.findById(connectionId)).thenReturn(Optional.of(connection));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> userConnectionService.rejectInvite(connectionId, wrongUserId));

        assertEquals("본인의 초대만 거절할 수 있습니다.", ex.getMessage());
    }

    @Test
    void 연결_종료_성공() {
        UUID connectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserAccount user = UserAccount.builder().id(userId).build();
        UserAccount partner = UserAccount.builder().id(UUID.randomUUID()).build();

        UserConnection connection = UserConnection.builder()
                .id(connectionId)
                .user(user)
                .partner(partner)
                .status(ConnectionStatus.CONNECTED)
                .startedAt(LocalDateTime.now())
                .build();

        when(userConnectionRepository.findById(connectionId)).thenReturn(Optional.of(connection));
        when(userConnectionRepository.save(any(UserConnection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userConnectionService.endConnection(connectionId, userId);

        assertEquals(ConnectionStatus.ENDED, connection.getStatus());
        assertNull(connection.getStartedAt());
        verify(userConnectionRepository, times(1)).save(connection);
    }

    @Test
    void 연결_종료_실패_본인이_아님() {
        UUID connectionId = UUID.randomUUID();
        UUID invalidUserId = UUID.randomUUID();

        UserAccount user = UserAccount.builder().id(UUID.randomUUID()).build();
        UserAccount partner = UserAccount.builder().id(UUID.randomUUID()).build();

        UserConnection connection = UserConnection.builder()
                .id(connectionId)
                .user(user)
                .partner(partner)
                .status(ConnectionStatus.CONNECTED)
                .build();

        when(userConnectionRepository.findById(connectionId)).thenReturn(Optional.of(connection));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> userConnectionService.endConnection(connectionId, invalidUserId));

        assertEquals("본인 연결만 종료할 수 있습니다.", ex.getMessage());
    }

    @Test
    void 이메일로_재결합_성공() {
        UUID userId = UUID.randomUUID();
        String partnerEmail = "partner@test.com";

        UserAccount user = UserAccount.builder().id(userId).build();
        UserAccount partner = UserAccount.builder().email(partnerEmail).build();

        UserConnection connection = UserConnection.builder()
                .user(user)
                .partner(partner)
                .status(ConnectionStatus.ENDED)
                .build();

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userAccountRepository.findByEmail(partnerEmail)).thenReturn(Optional.of(partner));
        when(userConnectionRepository.findByUserAndPartner(user, partner)).thenReturn(Optional.of(connection));
        when(userConnectionRepository.save(any(UserConnection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userConnectionService.reconnectByEmail(userId, partnerEmail);

        assertEquals(ConnectionStatus.CONNECTED, connection.getStatus());
        assertNotNull(connection.getStartedAt());
        verify(userConnectionRepository, times(1)).save(connection);
    }

    @Test
    void 이메일로_재결합_실패_상태불가() {
        UUID userId = UUID.randomUUID();
        String partnerEmail = "partner@test.com";

        UserAccount user = UserAccount.builder().id(userId).build();
        UserAccount partner = UserAccount.builder().email(partnerEmail).build();

        UserConnection connection = UserConnection.builder()
                .user(user)
                .partner(partner)
                .status(ConnectionStatus.CONNECTED)
                .build();

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userAccountRepository.findByEmail(partnerEmail)).thenReturn(Optional.of(partner));
        when(userConnectionRepository.findByUserAndPartner(user, partner)).thenReturn(Optional.of(connection));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userConnectionService.reconnectByEmail(userId, partnerEmail));

        assertEquals("현재 연결 상태로 재결합 불가합니다.", ex.getMessage());
    }

    @Test
    void 이메일로_재결합_실패_연결_없음() {
        UUID userId = UUID.randomUUID();
        String partnerEmail = "partner@test.com";

        UserAccount user = UserAccount.builder().id(userId).build();
        UserAccount partner = UserAccount.builder().email(partnerEmail).build();

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userAccountRepository.findByEmail(partnerEmail)).thenReturn(Optional.of(partner));
        when(userConnectionRepository.findByUserAndPartner(user, partner)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userConnectionService.reconnectByEmail(userId, partnerEmail));

        assertEquals("존재하지 않는 연결입니다.", ex.getMessage());
    }
}
