package me.jinheum.datelog.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.config.JwtProperties;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.exception.InvalidTokenException;
import me.jinheum.datelog.exception.TokenExpiredException;
import me.jinheum.datelog.repository.UserAccountRepository;
import me.jinheum.datelog.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final TokenService tokenService;
    private Key key;
    private final UserAccountRepository userAccountRepository;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generatedAccessToken(UUID id,String email) { //엑세스 토큰 생성
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenValidity().toMillis());

        return Jwts.builder()
                .setSubject(id.toString())
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generatedRefreshToken(UUID id,String email) { //리프레시 토큰 생성
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenValidity().toMillis());

        return Jwts.builder()
                .setSubject(id.toString())
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public void validateToken(String token) { //토큰 검증
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
        } catch (SecurityException | MalformedJwtException e) {
            throw new InvalidTokenException("유효하지 않은 서명입니다.");
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("지원되지 않는 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("클레임 문자열이 비어 있습니다.");
        }
    }

    public boolean isRefreshTokenValid(UUID userId, String refreshToken) {
        String storedToken = tokenService.getRefreshToken(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public String resolveAccessToken(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
    
    public String getEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    public UUID getUserId(String token) { //토큰에서 id 추출
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.getSubject());
    }
    

    private Claims extractAllClaims(String token) { //토큰에서 모든 클레임 추출
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String token) {
        String email = getEmail(token);
        UserAccount userAccount = userAccountRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("유저가 없습니다."));

        return new UsernamePasswordAuthenticationToken(userAccount, null, List.of());
    }

}