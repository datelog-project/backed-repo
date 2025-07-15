package me.jinheum.datelog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.exception.InvalidTokenException;
import me.jinheum.datelog.exception.TokenExpiredException;
import me.jinheum.datelog.service.TokenService;
import me.jinheum.datelog.util.CookieUtil;
import me.jinheum.datelog.util.ResponseUtil;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SignoutFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {
            
        if (request.getRequestURI().equals("/auth/signout") && request.getMethod().equals("POST")) {
            
            String token = jwtProvider.resolveAccessToken(request);
            if (token == null) {
                ResponseUtil.writeUnauthorizedResponse(response, "토큰이 존재하지 않거나 형식이 올바르지 않습니다");
                return;
            }

            try {
                jwtProvider.validateToken(token);  // AccessToken 검증
            } catch (TokenExpiredException | InvalidTokenException e) {
                ResponseUtil.writeUnauthorizedResponse(response, "유효하지 않거나 만료된 토큰");
                return;
            }

                UUID userId = jwtProvider.getUserId(token);
                tokenService.deleteRefreshToken(userId); //토큰 추출하고 id에 맞는 리프레시 토큰 redis에서 삭제

                ResponseCookie cookie = cookieUtil.deleteRefreshTokenCookie();

                response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json, charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"message\": \"로그아웃 완료\"}");
                return;
        }

        filterChain.doFilter(request, response);
    }
}