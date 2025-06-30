package me.jinheum.datelog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import me.jinheum.datelog.DTO.SignupRequest;
import me.jinheum.datelog.DTO.SignupResponse;
import me.jinheum.datelog.Entity.UserAccount;
import me.jinheum.datelog.Repository.UserAccountRepository;
import me.jinheum.datelog.Service.UserAccountService;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAccountService userAccountService;

    @Test
    void 회원가입_성공() {
        // given
        SignupRequest request = new SignupRequest("진흠", "jinhum@test.com", "1234");

        // 비밀번호 암호화
        String hashedPassword = "$2a$10$abc123";  // mock된 해시값
        Mockito.when(passwordEncoder.encode("1234")).thenReturn(hashedPassword);

        // 중복 없는 tag 생성
        Mockito.when(userAccountRepository.findByNameAndTag(eq("진흠"), anyString()))
                .thenReturn(Optional.empty());

        // 저장될 User 객체 (mock)
        UserAccount savedUser = UserAccount.builder()
                .id(UUID.randomUUID())
                .name("진흠")
                .tag("1234")
                .username("진흠#1234")
                .email("jinhum@test.com")
                .password(hashedPassword)
                .build();

        Mockito.when(userAccountRepository.save(any())).thenReturn(savedUser);

        // when
        SignupResponse response = userAccountService.signup(request);

        // then
        assertNotNull(response.id());
        assertEquals("진흠#1234", response.username());
    }
}
