package me.jinheum.datelog.Security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;

@Component
public class JwtProvider {
    
    @Value("${jwt.secret}")
    private String secretKey;

    private final Duration accessTokenValidity = Duration.ofMinutes(30);
    private final Duration refreshTokenValidity = Duration.ofDays(7);

    public String generatedAccessToken (UUID id, String username) {
        
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidity.toMillis());

        return Jwts.builder()
                .setSubject(id.toString())
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String generatedRefreshToken (UUID id) {
        
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidity.toMillis());

        return Jwts.builder()
            .setSubject(id.toString())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
            .compact();
    }
}
