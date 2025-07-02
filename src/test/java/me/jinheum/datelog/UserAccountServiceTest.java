package me.jinheum.datelog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import me.jinheum.datelog.DTO.SigninRequest;
import me.jinheum.datelog.DTO.SigninResponse;
import me.jinheum.datelog.DTO.SignupRequest;
import me.jinheum.datelog.DTO.SignupResponse;
import me.jinheum.datelog.Entity.UserAccount;
import me.jinheum.datelog.Repository.UserAccountRepository;
import me.jinheum.datelog.Security.JwtProvider;
import me.jinheum.datelog.Service.UserAccountService;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;


    @BeforeEach
    void setUp() {
        userAccountService = new UserAccountService(
            userAccountRepository,
            passwordEncoder,
            jwtProvider
        );
    }

    @InjectMocks
    private UserAccountService userAccountService;

    @Test
    void 회원가입_성공() {

        SignupRequest request = new SignupRequest("진흠", "jinhum@test.com", "1234");

        String hashedPassword = "$2a$10$abc123";
        Mockito.when(passwordEncoder.encode("1234")).thenReturn(hashedPassword);

        Mockito.when(userAccountRepository.findByNameAndTag(eq("진흠"), anyString()))
                .thenReturn(Optional.empty());

        UserAccount savedUser = UserAccount.builder()
                .id(UUID.randomUUID())
                .name("진흠")
                .tag("1234")
                .username("진흠#1234")
                .email("jinhum@test.com")
                .password(hashedPassword)
                .build();

        Mockito.when(userAccountRepository.save(any())).thenReturn(savedUser);

        SignupResponse response = userAccountService.signup(request);


        assertNotNull(response.id());
        assertEquals("진흠#1234", response.username());
    }

    @Test
    void 로그인_성공() {
        String rawPassword = "1234";
        String encodedPassword = "$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghi3";
        String email = "jinhum@test.com";

        UserAccount existingUser = UserAccount.builder()
                .id(UUID.randomUUID())
                .name("진흠")
                .tag("1234")
                .username("진흠#1234")
                .email(email)
                .password(encodedPassword)
                .build();

        Mockito.when(userAccountRepository.findByEmail(email))
                .thenReturn(Optional.of(existingUser));

        Mockito.when(passwordEncoder.matches(rawPassword, encodedPassword))
                .thenReturn(true);

        Mockito.when(jwtProvider.generatedAccessToken(Mockito.any(), Mockito.any()))
                .thenReturn("dummyAccessToken");

        Mockito.when(jwtProvider.generatedRefreshToken(Mockito.any()))
                .thenReturn("dummyRefreshToken");

        SigninRequest request = new SigninRequest(email, rawPassword);
        MockHttpServletResponse response = new MockHttpServletResponse();

        SigninResponse result = userAccountService.signin(request, response);

        assertNotNull(result);
        assertEquals(email, existingUser.getEmail());
        assertEquals(existingUser.getUsername(), result.username());
    }

}
