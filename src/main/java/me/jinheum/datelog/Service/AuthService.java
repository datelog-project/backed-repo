package me.jinheum.datelog.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.repository.UserAccountRepository;
import me.jinheum.datelog.security.JwtProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    private final Duration refreshTokenValidity = Duration.ofDays(7);

    public String getUsernameById(UUID userId) {
        return userAccountRepository.findById(userId)
                .map(UserAccount::getUsername)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
    }

    public String reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            throw new JwtException("유효하지 않은 refresh token");
        }

        UUID userId = jwtProvider.getUserId(refreshToken);

        if (!tokenService.isRefreshTokenValid(userId, refreshToken)) {
            throw new JwtException("저장된 refresh token과 일치하지 않음");
        }

        String username = getUsernameById(userId);
        String newAccessToken = jwtProvider.generatedAccessToken(userId, username);
        String newRefreshToken = jwtProvider.generatedRefreshToken(userId);
        tokenService.saveRefreshToken(userId, newRefreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenValidity.getSeconds())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());


        return newAccessToken;
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
