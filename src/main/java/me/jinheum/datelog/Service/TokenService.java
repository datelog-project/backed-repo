package me.jinheum.datelog.service;

import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.config.JwtProperties;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";

    private String buildKey(UUID userId) {
        return REFRESH_TOKEN_PREFIX + userId.toString();
    }

    public void saveRefreshToken(UUID userId, String refreshToken) {
        String key = buildKey(userId);
        redisTemplate.opsForValue().set(key, refreshToken, jwtProperties.getRefreshTokenValidity());
    }

    public String getRefreshToken(UUID userId) {
        return redisTemplate.opsForValue().get(buildKey(userId));
    }

    public void deleteRefreshToken(UUID userId) {
        redisTemplate.delete(buildKey(userId));
    }
    
}