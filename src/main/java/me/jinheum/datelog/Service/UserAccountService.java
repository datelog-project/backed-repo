package me.jinheum.datelog.Service;

import org.springframework.http.HttpHeaders;

import java.time.Duration;
import java.util.Random;

import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.DTO.SigninRequest;
import me.jinheum.datelog.DTO.SigninResponse;
import me.jinheum.datelog.DTO.SignupRequest;
import me.jinheum.datelog.DTO.SignupResponse;
import me.jinheum.datelog.Entity.UserAccount;
import me.jinheum.datelog.Repository.UserAccountRepository;
import me.jinheum.datelog.Security.JwtProvider;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final Duration refreshTokenValidity = Duration.ofDays(7);

    public SignupResponse signup(SignupRequest request) {
        if (userAccountRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        String tag = generatedUniqueTagForName(request.name());
        String username = request.name() + "#" + tag;

        String hashedPassword = passwordEncoder.encode(request.password());

        UserAccount user = UserAccount.builder()
                .name(request.name())
                .tag(tag)
                .username(username)
                .email(request.email())
                .password(hashedPassword)
                .build();

        UserAccount saved = userAccountRepository.save(user);
        return new SignupResponse(saved.getId(), saved.getUsername());
    }


    public String generatedUniqueTagForName(String name) {
        Random random = new Random();
        String tag;
        int maxAttempts = 1000;

        for (int i = 0; i < maxAttempts; i++) {
            int randNum = random.nextInt(9000) + 1000;
            tag = String.valueOf(randNum);
            boolean exists = userAccountRepository.findByNameAndTag(name, tag).isPresent();
            if (!exists) return tag;
        }
        throw new RuntimeException("중복으로 인해 유효한 tag 생성 실패");
    }

     public SigninResponse login(SigninRequest request, HttpServletResponse response) {
        UserAccount user = userAccountRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.generatedAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtProvider.generatedRefreshToken(user.getId());

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenValidity.getSeconds())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new SigninResponse(user.getId(), user.getUsername(), accessToken);
    }
}
