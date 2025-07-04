package me.jinheum.datelog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SignoutFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().equals("/auth/signout") && request.getMethod().equals("POST")) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                UUID userId = jwtProvider.getUserId(token);
                redisTemplate.delete("refreshToken:" + userId); //토큰 추출하고 id에 맞는 리프레시 토큰 redis에서 삭제

                ResponseCookie cookie = ResponseCookie.from("refreshToken", "") //쿠키에서도 리프레시 토큰 삭제
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(0)
                        .build();

                response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"sucess signout\"}");
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid Token\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}