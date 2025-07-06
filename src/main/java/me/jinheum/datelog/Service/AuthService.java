package me.jinheum.datelog.service;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.config.JwtProperties;
import me.jinheum.datelog.exception.InvalidTokenException;
import me.jinheum.datelog.security.JwtProvider;
import me.jinheum.datelog.util.CookieUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;
    private final CookieUtil cookieUtil;

    public String reissue(HttpServletRequest request, HttpServletResponse response) { //리프레시 토큰 검증해서 새로운 엑세스 토큰 발급
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh Token이 존재하지 않습니다.");
        }
        jwtProvider.validateToken(refreshToken);

        UUID userId = jwtProvider.getUserId(refreshToken);

        if (!jwtProvider.isRefreshTokenValid(userId, refreshToken)) { // 리프레시토큰 검증
            throw new JwtException("저장된 refresh token과 일치하지 않음");
        }

        String newAccessToken = jwtProvider.generatedAccessToken(userId);
        String newRefreshToken = jwtProvider.generatedRefreshToken(userId);
        tokenService.saveRefreshToken(userId, newRefreshToken);

        ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(refreshToken, jwtProperties.getRefreshTokenValidity());

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());


        return newAccessToken;
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) { //쿠키에서 리프레시 토큰 추출
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
