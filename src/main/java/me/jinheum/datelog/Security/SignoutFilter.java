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
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    jwtProvider.validateToken(token);  // AccessToken 검증
                } catch (TokenExpiredException | InvalidTokenException e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"유효하지 않거나 만료된 토큰\"}");
                    return;
                }

                UUID userId = jwtProvider.getUserId(token);
                tokenService.deleteRefreshToken(userId); //토큰 추출하고 id에 맞는 리프레시 토큰 redis에서 삭제

                ResponseCookie cookie = cookieUtil.deleteRefreshTokenCookie();

                response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"로그아웃 완료\"}");
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"유효하지 않은 토큰\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}