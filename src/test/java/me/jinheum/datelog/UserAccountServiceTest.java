package me.jinheum.datelog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import me.jinheum.datelog.config.JwtProperties;
import me.jinheum.datelog.dto.SigninRequest;
import me.jinheum.datelog.dto.SigninResponse;
import me.jinheum.datelog.dto.SignupRequest;
import me.jinheum.datelog.dto.SignupResponse;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.repository.UserAccountRepository;
import me.jinheum.datelog.security.JwtProvider;
import me.jinheum.datelog.service.AuthService;
import me.jinheum.datelog.service.UserAccountService;
import me.jinheum.datelog.util.CookieUtil;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private UserAccountService userAccountService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private AuthService authService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JwtProperties jwtProperties;

    @Test
    void 회원가입_성공() {

        SignupRequest request = new SignupRequest("진흠", "jinhum@test.com", "1234");

        String hashedPassword = "$2a$10$abc123";
        Mockito.when(passwordEncoder.encode("1234")).thenReturn(hashedPassword);


        UserAccount savedUser = UserAccount.builder()
                .id(UUID.randomUUID())
                .name("진흠")
                .email("jinhum@test.com")
                .password(hashedPassword)
                .build();

        Mockito.when(userAccountRepository.save(any())).thenReturn(savedUser);

        SignupResponse response = userAccountService.signup(request);


        assertNotNull(response.id());
    }

    @Test
    void 로그인_성공() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        
        when(cookieUtil.createRefreshTokenCookie(anyString(), any()))
            .thenReturn(ResponseCookie.from("refreshToken", "mockRefreshToken").build());

        when(jwtProperties.getRefreshTokenValidity()).thenReturn(Duration.ofDays(7));

        String rawPassword = "1234";
        String encodedPassword = "$2a$10$fakeHashHereFakeHashHere123456";
        String email = "jinheum@test.com";

        UserAccount user = UserAccount.builder()
            .id(UUID.randomUUID())
            .name("진흠")
            .email(email)
            .password(encodedPassword)
            .build();

        when(userAccountRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtProvider.generatedAccessToken(any(), any())).thenReturn("mockAccessToken");
        when(jwtProvider.generatedRefreshToken(any(), any())).thenReturn("mockRefreshToken");

        SigninRequest request = new SigninRequest(email, rawPassword);
        MockHttpServletResponse response = new MockHttpServletResponse();

        SigninResponse result = authService.signin(request, response);

        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals("mockAccessToken", result.accessToken());
    }
}
