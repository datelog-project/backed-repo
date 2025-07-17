package me.jinheum.datelog.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.exception.InvalidTokenException;
import me.jinheum.datelog.exception.TokenExpiredException;
import me.jinheum.datelog.util.ResponseUtil;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        if (path.startsWith("/auth/signin")
        || path.startsWith("/auth/signup")
        || path.startsWith("/auth/reissue")
        || path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-resources")
        || path.startsWith("/webjars")
        || path.equals("/swagger-ui.html")
        || path.equals("/error")
        || path.startsWith("/.well-known")
        || path.startsWith("/share/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtProvider.resolveAccessToken(request);
        
        if (token == null) {
            ResponseUtil.writeUnauthorizedResponse(response, "토큰이 존재하지 않거나 형식이 올바르지 않습니다.");
            return;
        }
        try {
            jwtProvider.validateToken(token);
            Authentication auth = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            
        } catch (TokenExpiredException e) {
            ResponseUtil.writeUnauthorizedResponse(response, "만료된 토큰입니다.");
            return;
        } catch (InvalidTokenException e) {
            ResponseUtil.writeUnauthorizedResponse(response, "유효하지 않은 토큰입니다.");
            return;
        } catch (UsernameNotFoundException e) {
            ResponseUtil.writeUnauthorizedResponse(response, "유저를 찾을 수 없습니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}