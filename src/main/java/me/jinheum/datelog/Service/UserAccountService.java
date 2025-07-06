package me.jinheum.datelog.service;

import org.springframework.http.HttpHeaders;


import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.config.JwtProperties;
import me.jinheum.datelog.dto.SigninRequest;
import me.jinheum.datelog.dto.SigninResponse;
import me.jinheum.datelog.dto.SignupRequest;
import me.jinheum.datelog.dto.SignupResponse;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.exception.EmailAlreadyExistsException;
import me.jinheum.datelog.exception.InvalidCredentialsException;
import me.jinheum.datelog.repository.UserAccountRepository;
import me.jinheum.datelog.security.JwtProvider;
import me.jinheum.datelog.util.CookieUtil;

import org.springframework.data.redis.core.StringRedisTemplate;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;
    private final CookieUtil cookieUtil;

    public SignupResponse signup(SignupRequest request) {
        if (userAccountRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        UserAccount user = UserAccount.builder()
                .name(request.name())
                .email(request.email())
                .password(hashedPassword)
                .build();

        UserAccount saved = userAccountRepository.save(user);
        return new SignupResponse(saved.getId());
    }


    public SigninResponse signin(SigninRequest request, HttpServletResponse response) {
        UserAccount user = userAccountRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.generatedAccessToken(user.getId());
        String refreshToken = jwtProvider.generatedRefreshToken(user.getId());

        String redisKey = "refreshToken:" + user.getId();
        redisTemplate.opsForValue().set(redisKey, refreshToken, jwtProperties.getRefreshTokenValidity());

        ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(refreshToken, jwtProperties.getRefreshTokenValidity());

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new SigninResponse(user.getId(), accessToken);
    }

}
